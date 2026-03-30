package com.ufc.tcc_gr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResult {
    private boolean allPassed;
    private int passedCount;
    private int totalCount;
    private List<CriterionResult> criteria;
}
