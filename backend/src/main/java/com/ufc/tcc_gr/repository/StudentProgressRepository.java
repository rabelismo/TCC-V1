package com.ufc.tcc_gr.repository;

import com.ufc.tcc_gr.model.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    List<StudentProgress> findByUserId(Long userId);
    Optional<StudentProgress> findByUserIdAndModuleId(Long userId, Long moduleId);
}
