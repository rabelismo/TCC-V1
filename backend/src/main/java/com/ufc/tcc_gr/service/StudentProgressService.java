package com.ufc.tcc_gr.service;

import com.ufc.tcc_gr.exception.ResourceNotFoundException;
import com.ufc.tcc_gr.model.Module;
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
    public StudentProgress recordSubmission(Long userId, Long moduleId, String code, boolean passed) {
        LocalDateTime now = LocalDateTime.now();
        StudentProgress progress = progressRepo.findByUserIdAndModuleId(userId, moduleId)
                .orElseGet(() -> StudentProgress.start(findUser(userId), findModule(moduleId), now));

        progress.recordSubmission(code, passed, now);
        return progressRepo.save(progress);
    }

    private User findUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
    }

    private Module findModule(Long moduleId) {
        return moduleRepo.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado: " + moduleId));
    }
}
