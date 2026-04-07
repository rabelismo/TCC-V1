package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.dto.ProgressUpdateRequest;
import com.ufc.tcc_gr.exception.ResourceNotFoundException;
import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.model.ProgressStatus;
import com.ufc.tcc_gr.model.StudentProgress;
import com.ufc.tcc_gr.model.User;
import com.ufc.tcc_gr.repository.ModuleRepository;
import com.ufc.tcc_gr.repository.StudentProgressRepository;
import com.ufc.tcc_gr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentProgressService {

    private final StudentProgressRepository progressRepo;
    private final UserRepository userRepo;
    private final ModuleRepository moduleRepo;

    public List<StudentProgress> getProgressByUser(Long userId) {
        return progressRepo.findByUserId(userId);
    }

    @Transactional
    public StudentProgress updateProgress(ProgressUpdateRequest request) {
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.getUserId()));
        Module module = moduleRepo.findById(request.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado: " + request.getModuleId()));

        StudentProgress progress = progressRepo
                .findByUserIdAndModuleId(request.getUserId(), request.getModuleId())
                .orElseGet(() -> {
                    StudentProgress sp = new StudentProgress(user, module);
                    sp.setStartedAt(LocalDateTime.now());
                    return sp;
                });

        if (request.getStatus() != null) {
            progress.setStatus(ProgressStatus.valueOf(request.getStatus()));
        }
        if (request.getCodeSnapshot() != null) {
            progress.setLastCodeSnapshot(request.getCodeSnapshot());
        }
        progress.setAttempts(progress.getAttempts() + 1);

        if (ProgressStatus.COMPLETED == progress.getStatus() && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        return progressRepo.save(progress);
    }
}
