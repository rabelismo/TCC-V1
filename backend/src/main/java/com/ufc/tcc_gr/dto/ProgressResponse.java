package com.ufc.tcc_gr.dto;

import com.ufc.tcc_gr.model.StudentProgress;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProgressResponse {
    private Long id;
    private Long userId;
    private ModuleRef module;
    private String status;
    private Integer attempts;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Data
    @AllArgsConstructor
    public static class ModuleRef {
        private Long id;
        private String title;
        private Integer orderIndex;
    }

    public static ProgressResponse from(StudentProgress sp) {
        return new ProgressResponse(
                sp.getId(),
                sp.getUser().getId(),
                new ModuleRef(sp.getModule().getId(), sp.getModule().getTitle(), sp.getModule().getOrderIndex()),
                sp.getStatus().name(),
                sp.getAttempts(),
                sp.getStartedAt(),
                sp.getCompletedAt()
        );
    }
}
