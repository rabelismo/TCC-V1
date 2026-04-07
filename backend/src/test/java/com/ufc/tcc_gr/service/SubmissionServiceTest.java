package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.dto.CodeExecutionRequest;
import com.ufc.tcc_gr.dto.CodeExecutionResponse;
import com.ufc.tcc_gr.dto.SubmissionRequest;
import com.ufc.tcc_gr.dto.SubmissionResult;
import com.ufc.tcc_gr.exception.ResourceNotFoundException;
import com.ufc.tcc_gr.model.AcceptanceCriterion;
import com.ufc.tcc_gr.model.CriterionType;
import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.repository.AcceptanceCriterionRepository;
import com.ufc.tcc_gr.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private AcceptanceCriterionRepository criterionRepo;

    @Mock
    private ModuleRepository moduleRepo;

    @Mock
    private PythonExecutionService pythonService;

    @Mock
    private StudentProgressService progressService;

    @InjectMocks
    private SubmissionService submissionService;

    private Module sampleModule;

    @BeforeEach
    void setUp() {
        sampleModule = Module.builder()
                .id(1L)
                .orderIndex(1)
                .title("Módulo 1")
                .theoryMarkdown("# Teoria")
                .starterCode("print('hello')")
                .build();
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando módulo não existe")
    void evaluate_moduleNotFound_throwsException() {
        when(moduleRepo.findById(99L)).thenReturn(Optional.empty());

        SubmissionRequest request = new SubmissionRequest(99L, null, "print('oi')");

        assertThatThrownBy(() -> submissionService.evaluate(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Deve retornar allPassed=false quando sem critérios")
    void evaluate_noCriteria_returnsEmptyResult() {
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(sampleModule));
        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L)).thenReturn(List.of());

        SubmissionResult result = submissionService.evaluate(
                new SubmissionRequest(1L, null, "print('hello')")
        );

        assertThat(result.isAllPassed()).isFalse();
        assertThat(result.getTotalCount()).isZero();
    }

    @Test
    @DisplayName("Deve passar critério OUTPUT_CONTAINS quando output contém o esperado")
    void evaluate_outputContains_passes() {
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(sampleModule));

        AcceptanceCriterion criterion = AcceptanceCriterion.builder()
                .id(10L)
                .orderIndex(1)
                .description("Output deve conter 'hello'")
                .type(CriterionType.OUTPUT_CONTAINS)
                .expectedOutput("hello")
                .hint("Use print('hello')")
                .hidden(false)
                .build();

        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(criterion));
        when(pythonService.execute(any(CodeExecutionRequest.class)))
                .thenReturn(new CodeExecutionResponse("hello world\n", "", 0, 50));

        SubmissionResult result = submissionService.evaluate(
                new SubmissionRequest(1L, null, "print('hello world')")
        );

        assertThat(result.isAllPassed()).isTrue();
        assertThat(result.getPassedCount()).isEqualTo(1);
        assertThat(result.getCriteria()).hasSize(1);
        assertThat(result.getCriteria().get(0).isPassed()).isTrue();
    }

    @Test
    @DisplayName("Deve falhar critério OUTPUT_EQUALS quando output difere")
    void evaluate_outputEquals_failsOnMismatch() {
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(sampleModule));

        AcceptanceCriterion criterion = AcceptanceCriterion.builder()
                .id(11L)
                .orderIndex(1)
                .description("Output deve ser exatamente 'abc'")
                .type(CriterionType.OUTPUT_EQUALS)
                .expectedOutput("abc")
                .hint("Dica: use print('abc')")
                .hidden(false)
                .build();

        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(criterion));
        when(pythonService.execute(any(CodeExecutionRequest.class)))
                .thenReturn(new CodeExecutionResponse("xyz\n", "", 0, 30));

        SubmissionResult result = submissionService.evaluate(
                new SubmissionRequest(1L, null, "print('xyz')")
        );

        assertThat(result.isAllPassed()).isFalse();
        assertThat(result.getPassedCount()).isZero();
        assertThat(result.getCriteria().get(0).getHint()).isEqualTo("Dica: use print('abc')");
    }

    @Test
    @DisplayName("Deve executar TEST_CASE com input específico")
    void evaluate_testCase_usesSpecificInput() {
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(sampleModule));

        AcceptanceCriterion criterion = AcceptanceCriterion.builder()
                .id(12L)
                .orderIndex(1)
                .description("Com input '5', output deve conter '25'")
                .type(CriterionType.TEST_CASE)
                .input("5")
                .expectedOutput("25")
                .hint("Eleve ao quadrado")
                .hidden(false)
                .build();

        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(criterion));

        when(pythonService.execute(argThat(req -> "5".equals(req.getInput()))))
                .thenReturn(new CodeExecutionResponse("25\n", "", 0, 20));

        SubmissionResult result = submissionService.evaluate(
                new SubmissionRequest(1L, null, "x = int(input()); print(x**2)")
        );

        assertThat(result.isAllPassed()).isTrue();
        verify(pythonService).execute(argThat(req -> "5".equals(req.getInput())));
    }

    @Test
    @DisplayName("Deve falhar todos os critérios quando execução retorna erro")
    void evaluate_executionError_failsAll() {
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(sampleModule));

        AcceptanceCriterion criterion = AcceptanceCriterion.builder()
                .id(13L)
                .orderIndex(1)
                .description("Output deve conter 'ok'")
                .type(CriterionType.OUTPUT_CONTAINS)
                .expectedOutput("ok")
                .hint("Corrija o código")
                .hidden(false)
                .build();

        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(criterion));
        when(pythonService.execute(any(CodeExecutionRequest.class)))
                .thenReturn(new CodeExecutionResponse("", "SyntaxError: invalid syntax", 1, 10));

        SubmissionResult result = submissionService.evaluate(
                new SubmissionRequest(1L, null, "print(")
        );

        assertThat(result.isAllPassed()).isFalse();
        assertThat(result.getCriteria().get(0).isPassed()).isFalse();
    }

    @Test
    @DisplayName("Deve atualizar progresso quando allPassed e userId presente")
    void evaluate_allPassed_updatesProgress() {
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(sampleModule));

        AcceptanceCriterion criterion = AcceptanceCriterion.builder()
                .id(14L)
                .orderIndex(1)
                .description("Output ok")
                .type(CriterionType.OUTPUT_CONTAINS)
                .expectedOutput("hello")
                .hidden(false)
                .build();

        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(criterion));
        when(pythonService.execute(any(CodeExecutionRequest.class)))
                .thenReturn(new CodeExecutionResponse("hello\n", "", 0, 10));

        submissionService.evaluate(new SubmissionRequest(1L, 1L, "print('hello')"));

        verify(progressService).updateProgress(any());
    }

    @Test
    @DisplayName("Não deve chamar progressService quando userId é null")
    void evaluate_noUserId_skipsProgress() {
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(sampleModule));

        AcceptanceCriterion criterion = AcceptanceCriterion.builder()
                .id(15L)
                .orderIndex(1)
                .description("Output ok")
                .type(CriterionType.OUTPUT_CONTAINS)
                .expectedOutput("hello")
                .hidden(false)
                .build();

        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(criterion));
        when(pythonService.execute(any(CodeExecutionRequest.class)))
                .thenReturn(new CodeExecutionResponse("hello\n", "", 0, 10));

        submissionService.evaluate(new SubmissionRequest(1L, null, "print('hello')"));

        verify(progressService, never()).updateProgress(any());
    }
}
