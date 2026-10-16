package com.ufc.tcc_gr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "progressList"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnoreProperties({"theoryMarkdown", "starterCode"})
    private Module module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(columnDefinition = "TEXT")
    private String lastCodeSnapshot;

    @Column(nullable = false)
    private Integer attempts = 0;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime updatedAt = LocalDateTime.now();

    private StudentProgress(User user, Module module, LocalDateTime startedAt) {
        this.user = user;
        this.module = module;
        this.startedAt = startedAt;
    }

    public static StudentProgress start(User user, Module module, LocalDateTime now) {
        return new StudentProgress(user, module, now);
    }

    public void recordSubmission(String code, boolean passed, LocalDateTime now) {
        attempts++;
        lastCodeSnapshot = code;
        if (passed) {
            complete(now);
        } else if (status == ProgressStatus.NOT_STARTED) {
            status = ProgressStatus.IN_PROGRESS;
        }
    }

    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }

    private void complete(LocalDateTime now) {
        status = ProgressStatus.COMPLETED;
        if (completedAt == null) {
            completedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
