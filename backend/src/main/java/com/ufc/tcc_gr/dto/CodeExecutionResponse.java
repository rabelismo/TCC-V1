package com.ufc.tcc_gr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResponse {
    private String output;
    private String error;
    private int exitCode;
    private long executionTimeMs;
}
