package com.ufc.tcc_gr.controller;

import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleRepository moduleRepo;

    @GetMapping
    public List<Module> getAllModules() {
        return moduleRepo.findAllByOrderByOrderIndexAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Module> getModule(@PathVariable Long id) {
        return moduleRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Module createModule(@RequestBody Module module) {
        return moduleRepo.save(module);
    }
}
