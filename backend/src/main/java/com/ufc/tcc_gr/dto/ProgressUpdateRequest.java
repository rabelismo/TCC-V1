package com.ufc.tcc_gr.dto;

import com.ufc.tcc_gr.model.ProgressStatus;
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

    private ProgressStatus status;

    private String codeSnapshot;
}
