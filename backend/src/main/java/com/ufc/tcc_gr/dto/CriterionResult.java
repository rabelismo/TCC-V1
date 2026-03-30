package com.ufc.tcc_gr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterionResult {
    private Long criterionId;
    private String description;
    private boolean passed;
    private String hint;
    private String actualOutput;
    private boolean hidden;
}
