package com.ufc.tcc_gr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module_id"}))
@Getter
@Setter
@NoArgsConstructor
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

    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public StudentProgress(User user, Module module) {
        this.user = user;
        this.module = module;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
