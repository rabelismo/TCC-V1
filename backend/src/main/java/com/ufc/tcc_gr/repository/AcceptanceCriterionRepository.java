package com.ufc.tcc_gr.repository;

import com.ufc.tcc_gr.model.AcceptanceCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcceptanceCriterionRepository extends JpaRepository<AcceptanceCriterion, Long> {
    List<AcceptanceCriterion> findByModuleIdOrderByOrderIndexAsc(Long moduleId);
}
