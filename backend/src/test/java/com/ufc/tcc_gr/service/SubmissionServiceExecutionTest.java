package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.dto.CodeExecutionRequest;
import com.ufc.tcc_gr.dto.CodeExecutionResponse;
import com.ufc.tcc_gr.dto.SubmissionRequest;
import com.ufc.tcc_gr.dto.SubmissionResult;
import com.ufc.tcc_gr.exception.ExecutionCapacityException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceExecutionTest {

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

    @BeforeEach
    void setUp() {
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(Module.builder().id(1L).build()));
    }

    private AcceptanceCriterion testCase(long id, String input) {
        return AcceptanceCriterion.builder()
                .id(id)
                .orderIndex((int) id)
                .description("Caso " + id)
                .type(CriterionType.TEST_CASE)
                .input(input)
                .expectedOutput("ok")
                .hint("dica " + id)
                .hidden(false)
                .build();
    }

    @Test
    @DisplayName("Falta de capacidade propaga e não registra tentativa")
    void evaluate_capacityExhausted_propagatesWithoutRecording() {
        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(testCase(1, "a")));
        when(pythonService.execute(any(CodeExecutionRequest.class)))
                .thenThrow(new ExecutionCapacityException("ocupado"));

        assertThatThrownBy(() -> submissionService.evaluate(7L, new SubmissionRequest(1L, "print('ok')")))
                .isInstanceOf(ExecutionCapacityException.class);
        verifyNoInteractions(progressService);
    }

    @Test
    @DisplayName("Esgotado o orçamento de tempo, critérios restantes não executam e reprovam")
    void evaluate_budgetExhausted_skipsRemainingRuns() {
        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(testCase(1, "a"), testCase(2, "b"), testCase(3, "c"), testCase(4, "d")));
        when(pythonService.execute(any(CodeExecutionRequest.class)))
                .thenReturn(new CodeExecutionResponse("", "Erro: Tempo limite excedido (10s).", 124, 10_000));

        SubmissionResult result = submissionService.evaluate(7L, new SubmissionRequest(1L, "while True: pass"));

        verify(pythonService, times(2)).execute(any(CodeExecutionRequest.class));
        assertThat(result.getCriteria()).hasSize(4);
        assertThat(result.getCriteria().subList(2, 4))
                .allSatisfy(c -> {
                    assertThat(c.isPassed()).isFalse();
                    assertThat(c.getHint()).isEqualTo(SubmissionService.BUDGET_EXHAUSTED_HINT);
                });
        verify(progressService).recordSubmission(7L, 1L, "while True: pass", false);
    }

    @Test
    @DisplayName("Execuções rápidas avaliam todos os critérios")
    void evaluate_fastRuns_evaluateEverything() {
        when(criterionRepo.findByModuleIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(testCase(1, "a"), testCase(2, "b"), testCase(3, "c"), testCase(4, "d")));
        when(pythonService.execute(any(CodeExecutionRequest.class)))
                .thenReturn(new CodeExecutionResponse("ok\n", "", 0, 40));

        SubmissionResult result = submissionService.evaluate(7L, new SubmissionRequest(1L, "print('ok')"));

        verify(pythonService, times(4)).execute(any(CodeExecutionRequest.class));
        assertThat(result.isAllPassed()).isTrue();
    }
}
