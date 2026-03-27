package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.dto.CodeExecutionRequest;
import com.ufc.tcc_gr.dto.CodeExecutionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PythonExecutionService {

    private static final Set<String> BLOCKED_MODULES = Set.of(
        "os", "sys", "subprocess", "shutil", "socket", "http",
        "urllib", "requests", "ctypes", "signal", "threading",
        "multiprocessing", "pickle", "shelve", "importlib",
        "code", "codeop", "compile", "compileall", "webbrowser"
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
        Pattern.compile("compile\\s*\\(")
    );

    @Value("${python.execution.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${python.execution.command:python}")
    private String pythonCommand;

    public CodeExecutionResponse execute(CodeExecutionRequest request) {
        String code = request.getCode();

        String validationError = validateCode(code);
        if (validationError != null) {
            return new CodeExecutionResponse("", validationError, 1, 0);
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("tcc_exec_", ".py");
            Files.writeString(tempFile, code, StandardCharsets.UTF_8);

            long startTime = System.currentTimeMillis();

            ProcessBuilder pb = new ProcessBuilder(pythonCommand, tempFile.toAbsolutePath().toString());
            pb.redirectErrorStream(false);
            pb.environment().put("PYTHONDONTWRITEBYTECODE", "1");

            Process process = pb.start();

            if (request.getInput() != null && !request.getInput().isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(request.getInput().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            String stdout = readStream(process.getInputStream());
            String stderr = readStream(process.getErrorStream());

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                return new CodeExecutionResponse(
                    stdout,
                    "Erro: Tempo limite excedido (" + timeoutSeconds + "s). Verifique se há loops infinitos.",
                    124,
                    elapsed
                );
            }

            return new CodeExecutionResponse(stdout, stderr, process.exitValue(), elapsed);

        } catch (Exception e) {
            log.error("Erro ao executar código Python", e);
            return new CodeExecutionResponse("", "Erro interno do servidor: " + e.getMessage(), 1, 0);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
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
                "(^|\\n)\\s*(import\\s+" + module + "|from\\s+" + module + ")"
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

    private String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
