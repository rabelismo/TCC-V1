package com.ufc.tcc_gr.config;

import com.ufc.tcc_gr.model.CriterionType;
import lombok.Data;

import java.util.List;

@Data
public class ModuleData {
    private Integer orderIndex;
    private String title;
    private String concept;
    private String description;
    private String theoryMarkdown;
    private String starterCode;
    private String expectedOutput;
    private String sampleInput;
    private List<CriterionData> criteria;

    @Data
    public static class CriterionData {
        private Integer orderIndex;
        private CriterionType type;
        private String description;
        private String input;
        private String expectedOutput;
        private String hint;
        private Boolean hidden = false;
    }
}
