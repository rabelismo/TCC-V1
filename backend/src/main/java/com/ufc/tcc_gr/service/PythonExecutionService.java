package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.dto.CodeExecutionRequest;
import com.ufc.tcc_gr.dto.CodeExecutionResponse;
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
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PythonExecutionService {

    private static final int MAX_OUTPUT_BYTES = 50 * 1024;
    private static final int MAX_CONCURRENT_EXECUTIONS = 5;

    private final Semaphore executionSemaphore = new Semaphore(MAX_CONCURRENT_EXECUTIONS);
    private final ExecutorService streamReaderPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "python-stream-reader");
        t.setDaemon(true);
        return t;
    });

    private static final Set<String> BLOCKED_MODULES = Set.of(
        "os", "sys", "subprocess", "shutil", "socket", "http",
        "urllib", "requests", "ctypes", "signal", "threading",
        "multiprocessing", "pickle", "shelve", "importlib",
        "code", "codeop", "compile", "compileall", "webbrowser",
        "pathlib", "tempfile", "glob", "fnmatch", "io",
        "builtins", "gc", "inspect", "ast", "dis",
        "runpy", "pkgutil", "zipimport"
    );

    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
        Pattern.compile("__import__\\s*\\("),
        Pattern.compile("exec\\s*\\("),
        Pattern.compile("eval\\s*\\("),
        Pattern.compile("open\\s*\\("),
        Pattern.compile("globals\\s*\\("),
        Pattern.compile("locals\\s*\\("),
        Pattern.compile("getattr\\s*\\("),
        Pattern.compile("setattr\\s*\\("),
        Pattern.compile("delattr\\s*\\("),
        Pattern.compile("compile\\s*\\("),
        Pattern.compile("vars\\s*\\("),
        Pattern.compile("dir\\s*\\(\\s*\\)"),
        Pattern.compile("__builtins__"),
        Pattern.compile("__subclasses__"),
        Pattern.compile("__class__"),
        Pattern.compile("__bases__"),
        Pattern.compile("__mro__"),
        Pattern.compile("breakpoint\\s*\\(")
    );

    @Value("${python.execution.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${python.execution.command:python}")
    private String pythonCommand;

    private String sandboxWrapper;

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
            return new CodeExecutionResponse("",
                "Servidor ocupado — muitas execuções simultâneas. Tente novamente em instantes.", 1, 0);
        }

        Path tempFile = null;
        Process process = null;
        try {
            tempFile = Files.createTempFile("tcc_exec_", ".py");
            String sandboxedCode = sandboxWrapper + "\n" + code;
            Files.writeString(tempFile, sandboxedCode, StandardCharsets.UTF_8);

            long startTime = System.currentTimeMillis();

            ProcessBuilder pb = new ProcessBuilder(pythonCommand, "-B", "-S",
                    tempFile.toAbsolutePath().toString());
            pb.redirectErrorStream(false);
            pb.environment().put("PYTHONDONTWRITEBYTECODE", "1");
            pb.environment().put("PYTHONNOUSERSITE", "1");
            pb.environment().put("PYTHONUTF8", "1");
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            process = pb.start();

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

        } catch (Exception e) {
            log.error("Erro ao executar código Python", e);
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            return new CodeExecutionResponse("", "Erro interno ao executar código.", 1, 0);
        } finally {
            executionSemaphore.release();
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }
    }

    private static String getQuietly(Future<String> future) {
        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "";
        }
    }

    private String validateCode(String code) {
        if (code == null || code.isBlank()) {
            return "Erro: Código vazio. Escreva seu código Python antes de executar.";
        }

        if (code.length() > 10_000) {
            return "Erro: Código muito longo (máximo 10.000 caracteres).";
        }

        for (String module : BLOCKED_MODULES) {
            Pattern importPattern = Pattern.compile(
                "(^|\\n)\\s*(import\\s+" + module + "\\b|from\\s+" + module + "\\b)"
            );
            if (importPattern.matcher(code).find()) {
                return "Erro de segurança: O módulo '" + module + "' não é permitido nesta plataforma.";
            }
        }

        for (Pattern pattern : BLOCKED_PATTERNS) {
            if (pattern.matcher(code).find()) {
                return "Erro de segurança: Função bloqueada detectada. "
                     + "Use apenas print(), input() e funções matemáticas básicas.";
            }
        }

        return null;
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
