package com.ufc.tcc_gr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {
    private Long moduleId;
    private Long userId;
    private String code;
}
