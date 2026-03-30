package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.dto.*;
import com.ufc.tcc_gr.model.AcceptanceCriterion;
import com.ufc.tcc_gr.repository.AcceptanceCriterionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final AcceptanceCriterionRepository criterionRepo;
    private final PythonExecutionService pythonService;
    private final StudentProgressService progressService;

    public SubmissionResult evaluate(SubmissionRequest request) {
        List<AcceptanceCriterion> criteria = criterionRepo
                .findByModuleIdOrderByOrderIndexAsc(request.getModuleId());

        if (criteria.isEmpty()) {
            return SubmissionResult.builder()
                    .allPassed(false)
                    .passedCount(0)
                    .totalCount(0)
                    .criteria(List.of())
                    .build();
        }

        CodeExecutionResponse defaultRun = null;

        List<CriterionResult> results = new ArrayList<>();
        int passedCount = 0;

        for (AcceptanceCriterion criterion : criteria) {
            CodeExecutionResponse execution;

            if ("TEST_CASE".equals(criterion.getType()) && criterion.getInput() != null) {
                execution = pythonService.execute(
                        new CodeExecutionRequest(request.getCode(), criterion.getInput()));
            } else {
                if (defaultRun == null) {
                    defaultRun = pythonService.execute(
                            new CodeExecutionRequest(request.getCode(), null));
                }
                execution = defaultRun;
            }

            boolean passed = evaluateCriterion(criterion, execution);
            if (passed) passedCount++;

            results.add(CriterionResult.builder()
                    .criterionId(criterion.getId())
                    .description(criterion.getHidden() && !passed
                            ? "Critério oculto"
                            : criterion.getDescription())
                    .passed(passed)
                    .hint(passed ? null : criterion.getHint())
                    .actualOutput(execution.getExitCode() != 0
                            ? execution.getError().trim()
                            : truncate(execution.getOutput().trim(), 500))
                    .hidden(criterion.getHidden())
                    .build());
        }

        boolean allPassed = passedCount == criteria.size();

        if (allPassed && request.getUserId() != null) {
            try {
                progressService.updateProgress(new ProgressUpdateRequest(
                        request.getUserId(),
                        request.getModuleId(),
                        "COMPLETED",
                        request.getCode()
                ));
            } catch (Exception e) {
                log.warn("Não foi possível atualizar progresso do aluno: {}", e.getMessage());
            }
        }

        return SubmissionResult.builder()
                .allPassed(allPassed)
                .passedCount(passedCount)
                .totalCount(criteria.size())
                .criteria(results)
                .build();
    }

    private boolean evaluateCriterion(AcceptanceCriterion criterion, CodeExecutionResponse execution) {
        if (execution.getExitCode() != 0) return false;

        String actual = execution.getOutput().trim();
        String expected = criterion.getExpectedOutput().trim();

        return switch (criterion.getType()) {
            case "OUTPUT_CONTAINS" -> actual.contains(expected);
            case "OUTPUT_EQUALS" -> actual.equals(expected);
            case "TEST_CASE" -> actual.contains(expected);
            default -> false;
        };
    }

    private String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}
