package com.ufc.tcc_gr.repository;

import com.ufc.tcc_gr.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findAllByOrderByOrderIndexAsc();
    List<Module> findByConcept(String concept);
}
