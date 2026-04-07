package com.ufc.tcc_gr.dto;

import com.ufc.tcc_gr.model.Module;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModuleResponse {
    private Long id;
    private Integer orderIndex;
    private String title;
    private String description;
    private String theoryMarkdown;
    private String starterCode;
    private String expectedOutput;
    private String concept;
    private String sampleInput;

    public static ModuleResponse from(Module m) {
        return new ModuleResponse(
                m.getId(), m.getOrderIndex(), m.getTitle(), m.getDescription(),
                m.getTheoryMarkdown(), m.getStarterCode(), m.getExpectedOutput(),
                m.getConcept(), m.getSampleInput()
        );
    }
}
