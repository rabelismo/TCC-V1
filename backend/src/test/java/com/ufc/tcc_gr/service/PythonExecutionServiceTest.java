package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.dto.CodeExecutionRequest;
import com.ufc.tcc_gr.dto.CodeExecutionResponse;
import com.ufc.tcc_gr.exception.ExecutionCapacityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PythonExecutionServiceTest {

    private PythonExecutionService service;

    @BeforeEach
    void setUp() {
        service = new PythonExecutionService(10, "python", 5, 256);
        service.loadSandboxWrapper();
    }

    @Test
    @DisplayName("Processo Python não herda variáveis de ambiente do servidor")
    void buildProcess_doesNotLeakServerEnvironment() {
        var env = service.buildProcess(Path.of("x.py")).environment();

        assertThat(env.keySet())
                .allMatch(key -> PythonExecutionService.INHERITED_ENV.contains(key) || key.startsWith("PYTHON"));
        assertThat(env).containsEntry("PYTHONUTF8", "1");
    }

    @Test
    @DisplayName("Executa programa com input usando o ambiente reduzido")
    void execute_runsWithReducedEnvironment() {
        CodeExecutionResponse response = service.execute(
                new CodeExecutionRequest("nome = input()\nprint(f'Olá, {nome}')", "Ana"));

        assertThat(response.getError()).isEmpty();
        assertThat(response.getExitCode()).isZero();
        assertThat(response.getOutput().trim()).isEqualTo("Olá, Ana");
    }

    @Test
    @DisplayName("Sem capacidade, lança ExecutionCapacityException em vez de reprovar o código")
    void execute_withoutCapacity_throws() {
        PythonExecutionService busy = new PythonExecutionService(10, "python", 0, 256);

        assertThatThrownBy(() -> busy.execute(new CodeExecutionRequest("print(1)", null)))
                .isInstanceOf(ExecutionCapacityException.class);
    }

    @Test
    @DisplayName("Falha de infraestrutura vira exceção, não um resultado de execução do aluno")
    void execute_missingInterpreter_throws() {
        PythonExecutionService broken = new PythonExecutionService(10, "python-inexistente-xyz", 5, 256);

        assertThatThrownBy(() -> broken.execute(new CodeExecutionRequest("print(1)", null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("infraestrutura");
    }

    @Test
    @DisplayName("Qualquer import recebe mensagem clara antes de executar")
    void validateCode_rejectsAnyImport() {
        assertThat(service.validateCode("import random")).contains("importações não estão disponíveis");
        assertThat(service.validateCode("from math import sqrt")).contains("importações não estão disponíveis");
        assertThat(service.validateCode("    import os")).contains("importações não estão disponíveis");
    }

    @Test
    @DisplayName("Nomes que só contêm uma função bloqueada não são rejeitados")
    void validateCode_doesNotFlagSubstrings() {
        assertThat(service.validateCode("def reopen():\n    return 1\nprint(reopen())")).isNull();
        assertThat(service.validateCode("def my_eval(x):\n    return x\nprint(my_eval(2))")).isNull();
        assertThat(service.validateCode("print('importante')")).isNull();
    }

    @Test
    @DisplayName("Funções perigosas continuam bloqueadas")
    void validateCode_blocksDangerousCalls() {
        assertThat(service.validateCode("open('x')")).contains("Função bloqueada");
        assertThat(service.validateCode("eval ('1')")).contains("Função bloqueada");
        assertThat(service.validateCode("().__class__")).contains("Função bloqueada");
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    @DisplayName("Alocação acima do limite de memória falha só no processo do aluno")
    void execute_memoryLimit_raisesMemoryError() {
        CodeExecutionResponse response = service.execute(
                new CodeExecutionRequest("s = 'a' * (10 ** 9)\nprint(len(s))", null));

        assertThat(response.getExitCode()).isNotZero();
        assertThat(response.getError()).contains("MemoryError");
    }
}
