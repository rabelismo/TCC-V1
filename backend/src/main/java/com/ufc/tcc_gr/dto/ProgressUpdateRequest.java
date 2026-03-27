package com.ufc.tcc_gr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdateRequest {
    private Long userId;
    private Long moduleId;
    private String status;
    private String codeSnapshot;
}
