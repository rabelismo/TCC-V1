package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.dto.CodeExecutionRequest;
import com.ufc.tcc_gr.dto.CodeExecutionResponse;
import com.ufc.tcc_gr.exception.ExecutionCapacityException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
public class PythonExecutionService {

    private static final int MAX_OUTPUT_BYTES = 50 * 1024;
    private static final int MAX_CODE_LENGTH = 10_000;

    static final Set<String> INHERITED_ENV = Set.of("PATH", "Path", "SYSTEMROOT", "SystemRoot");

    private static final Map<String, String> PYTHON_ENV = Map.of(
        "PYTHONDONTWRITEBYTECODE", "1",
        "PYTHONNOUSERSITE", "1",
        "PYTHONUTF8", "1",
        "PYTHONIOENCODING", "utf-8"
    );

    private static final Pattern IMPORT_STATEMENT =
        Pattern.compile("(?m)^\\s*(import\\s+\\w|from\\s+[\\w.]+\\s+import\\b)");

    private static final List<Pattern> BLOCKED_PATTERNS = Stream.concat(
        Stream.of("__import__", "exec", "eval", "open", "globals", "locals", "getattr",
                  "setattr", "delattr", "compile", "vars", "breakpoint")
            .map(name -> Pattern.compile("\\b" + name + "\\s*\\(")),
        Stream.of("\\bdir\\s*\\(\\s*\\)", "__builtins__", "__subclasses__", "__class__",
                  "__bases__", "__mro__")
            .map(Pattern::compile)
    ).toList();

    private final int timeoutSeconds;
    private final String pythonCommand;
    private final int memoryLimitMb;
    private final Semaphore executionSemaphore;
    private final ExecutorService streamReaderPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "python-stream-reader");
        t.setDaemon(true);
        return t;
    });

    private String sandboxWrapper;

    public PythonExecutionService(
            @Value("${python.execution.timeout-seconds:10}") int timeoutSeconds,
            @Value("${python.execution.command:python}") String pythonCommand,
            @Value("${python.execution.max-concurrent:5}") int maxConcurrent,
            @Value("${python.execution.memory-limit-mb:256}") int memoryLimitMb) {
        this.timeoutSeconds = timeoutSeconds;
        this.pythonCommand = pythonCommand;
        this.memoryLimitMb = memoryLimitMb;
        this.executionSemaphore = new Semaphore(maxConcurrent);
    }

    @PostConstruct
    void loadSandboxWrapper() {
        try {
            ClassPathResource resource = new ClassPathResource("sandbox_wrapper.py");
            sandboxWrapper = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.info("Sandbox wrapper Python carregado ({} bytes)", sandboxWrapper.length());
        } catch (IOException e) {
            log.warn("sandbox_wrapper.py não encontrado — sandbox desabilitada", e);
            sandboxWrapper = "";
        }
    }

    public CodeExecutionResponse execute(CodeExecutionRequest request) {
        String code = request.getCode();

        String validationError = validateCode(code);
        if (validationError != null) {
            return new CodeExecutionResponse("", validationError, 1, 0);
        }

        if (!executionSemaphore.tryAcquire()) {
            throw new ExecutionCapacityException(
                "Servidor ocupado — muitas execuções simultâneas. Tente novamente em instantes.");
        }

        Path tempFile = null;
        Process process = null;
        try {
            tempFile = Files.createTempFile("tcc_exec_", ".py");
            Files.writeString(tempFile, sandboxWrapper + "\n" + code, StandardCharsets.UTF_8);

            long startTime = System.currentTimeMillis();
            process = buildProcess(tempFile).start();

            if (request.getInput() != null && !request.getInput().isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(request.getInput().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            } else {
                process.getOutputStream().close();
            }

            final Process proc = process;
            Future<String> stdoutFuture = streamReaderPool.submit(
                    () -> readStream(proc.getInputStream(), MAX_OUTPUT_BYTES));
            Future<String> stderrFuture = streamReaderPool.submit(
                    () -> readStream(proc.getErrorStream(), MAX_OUTPUT_BYTES));

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                process.waitFor(2, TimeUnit.SECONDS);
                String partialOut = getQuietly(stdoutFuture);
                return new CodeExecutionResponse(
                    partialOut,
                    "Erro: Tempo limite excedido (" + timeoutSeconds + "s). Verifique se há loops infinitos ou input() sem dados.",
                    124,
                    elapsed
                );
            }

            String stdout = stdoutFuture.get(3, TimeUnit.SECONDS);
            String stderr = stderrFuture.get(3, TimeUnit.SECONDS);

            return new CodeExecutionResponse(stdout, stderr, process.exitValue(), elapsed);

        } catch (IOException | ExecutionException | TimeoutException e) {
            destroy(process);
            throw new IllegalStateException("Falha de infraestrutura ao executar Python", e);
        } catch (InterruptedException e) {
            destroy(process);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execução Python interrompida", e);
        } finally {
            executionSemaphore.release();
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }
    }

    private static void destroy(Process process) {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    ProcessBuilder buildProcess(Path script) {
        ProcessBuilder pb = new ProcessBuilder(pythonCommand, "-B", "-S",
                script.toAbsolutePath().toString(),
                String.valueOf(memoryLimitMb), String.valueOf(timeoutSeconds));
        pb.redirectErrorStream(false);
        Map<String, String> env = pb.environment();
        env.keySet().retainAll(INHERITED_ENV);
        env.putAll(PYTHON_ENV);
        return pb;
    }

    String validateCode(String code) {
        if (code == null || code.isBlank()) {
            return "Erro: Código vazio. Escreva seu código Python antes de executar.";
        }

        if (code.length() > MAX_CODE_LENGTH) {
            return "Erro: Código muito longo (máximo 10.000 caracteres).";
        }

        if (IMPORT_STATEMENT.matcher(code).find()) {
            return "Erro: importações não estão disponíveis nesta plataforma. "
                 + "Use apenas as funções nativas do Python, como print(), input(), len() e range().";
        }

        for (Pattern pattern : BLOCKED_PATTERNS) {
            if (pattern.matcher(code).find()) {
                return "Erro de segurança: Função bloqueada detectada. "
                     + "Use apenas print(), input() e funções matemáticas básicas.";
            }
        }

        return null;
    }

    private static String getQuietly(Future<String> future) {
        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "";
        }
    }

    private String readStream(InputStream is, int maxBytes) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            int totalRead = 0;
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                int remaining = maxBytes - totalRead;
                if (remaining <= 0) {
                    sb.append("\n... [saída truncada - limite de ")
                      .append(maxBytes / 1024).append("KB atingido]");
                    break;
                }
                int toAppend = Math.min(charsRead, remaining);
                sb.append(buffer, 0, toAppend);
                totalRead += toAppend;
            }
            return sb.toString();
        }
    }
}
