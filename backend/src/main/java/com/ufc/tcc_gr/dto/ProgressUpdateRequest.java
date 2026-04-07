package com.ufc.tcc_gr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdateRequest {

    @NotNull(message = "O userId é obrigatório")
    private Long userId;

    @NotNull(message = "O moduleId é obrigatório")
    private Long moduleId;

    private String status;

    private String codeSnapshot;
}
