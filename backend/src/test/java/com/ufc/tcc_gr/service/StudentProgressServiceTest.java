package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.exception.ResourceNotFoundException;
import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.model.ProgressStatus;
import com.ufc.tcc_gr.model.StudentProgress;
import com.ufc.tcc_gr.model.User;
import com.ufc.tcc_gr.repository.ModuleRepository;
import com.ufc.tcc_gr.repository.StudentProgressRepository;
import com.ufc.tcc_gr.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProgressServiceTest {

    @Mock
    private StudentProgressRepository progressRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ModuleRepository moduleRepo;

    @InjectMocks
    private StudentProgressService service;

    @Test
    @DisplayName("Primeira submissão cria o progresso do aluno")
    void recordSubmission_createsProgressOnFirstSubmission() {
        when(progressRepo.findByUserIdAndModuleId(7L, 1L)).thenReturn(Optional.empty());
        when(userRepo.findById(7L)).thenReturn(Optional.of(new User("a@b.c", "Ana", "hash")));
        when(moduleRepo.findById(1L)).thenReturn(Optional.of(new Module()));
        when(progressRepo.save(any(StudentProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentProgress progress = service.recordSubmission(7L, 1L, "print(1)", true);

        assertThat(progress.getStatus()).isEqualTo(ProgressStatus.COMPLETED);
        assertThat(progress.getAttempts()).isEqualTo(1);
        assertThat(progress.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("Submissões seguintes reaproveitam o progresso existente")
    void recordSubmission_reusesExistingProgress() {
        StudentProgress existing = StudentProgress.start(
                new User("a@b.c", "Ana", "hash"), new Module(), LocalDateTime.now());
        existing.recordSubmission("v1", false, LocalDateTime.now());
        when(progressRepo.findByUserIdAndModuleId(7L, 1L)).thenReturn(Optional.of(existing));
        when(progressRepo.save(existing)).thenReturn(existing);

        StudentProgress progress = service.recordSubmission(7L, 1L, "v2", false);

        assertThat(progress).isSameAs(existing);
        assertThat(progress.getAttempts()).isEqualTo(2);
        assertThat(progress.getStatus()).isEqualTo(ProgressStatus.IN_PROGRESS);
        verifyNoInteractions(userRepo, moduleRepo);
    }

    @Test
    @DisplayName("Usuário inexistente gera ResourceNotFoundException e nada é salvo")
    void recordSubmission_unknownUser_throws() {
        when(progressRepo.findByUserIdAndModuleId(99L, 1L)).thenReturn(Optional.empty());
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordSubmission(99L, 1L, "x", true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(progressRepo, never()).save(any());
    }
}
