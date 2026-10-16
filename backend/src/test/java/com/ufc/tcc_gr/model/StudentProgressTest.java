package com.ufc.tcc_gr.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StudentProgressTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime T1 = T0.plusHours(1);
    private static final LocalDateTime T2 = T0.plusHours(2);

    private StudentProgress fresh() {
        return StudentProgress.start(new User("a@b.c", "Ana", "hash"), new Module(), T0);
    }

    @Test
    @DisplayName("Começa como NOT_STARTED, sem tentativas e com startedAt")
    void start_initialState() {
        StudentProgress progress = fresh();

        assertThat(progress.getStatus()).isEqualTo(ProgressStatus.NOT_STARTED);
        assertThat(progress.getAttempts()).isZero();
        assertThat(progress.getStartedAt()).isEqualTo(T0);
        assertThat(progress.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("Submissão reprovada conta tentativa e passa para IN_PROGRESS")
    void failedSubmission_movesToInProgress() {
        StudentProgress progress = fresh();

        progress.recordSubmission("print(1)", false, T1);

        assertThat(progress.getStatus()).isEqualTo(ProgressStatus.IN_PROGRESS);
        assertThat(progress.getAttempts()).isEqualTo(1);
        assertThat(progress.getLastCodeSnapshot()).isEqualTo("print(1)");
        assertThat(progress.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("Submissão aprovada conclui e grava completedAt")
    void passedSubmission_completes() {
        StudentProgress progress = fresh();

        progress.recordSubmission("print(1)", false, T1);
        progress.recordSubmission("print(2)", true, T2);

        assertThat(progress.isCompleted()).isTrue();
        assertThat(progress.getAttempts()).isEqualTo(2);
        assertThat(progress.getCompletedAt()).isEqualTo(T2);
    }

    @Test
    @DisplayName("Módulo concluído não regride e mantém o primeiro completedAt")
    void completed_neverRegresses() {
        StudentProgress progress = fresh();

        progress.recordSubmission("ok", true, T1);
        progress.recordSubmission("quebrado", false, T2);
        progress.recordSubmission("ok de novo", true, T2);

        assertThat(progress.isCompleted()).isTrue();
        assertThat(progress.getCompletedAt()).isEqualTo(T1);
        assertThat(progress.getAttempts()).isEqualTo(3);
        assertThat(progress.getLastCodeSnapshot()).isEqualTo("ok de novo");
    }
}
