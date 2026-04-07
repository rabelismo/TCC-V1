package com.ufc.tcc_gr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {

    @NotNull(message = "O moduleId é obrigatório")
    private Long moduleId;

    private Long userId;

    @NotBlank(message = "O código não pode estar vazio")
    private String code;
}
