package com.ufc.tcc_gr.controller;

import com.ufc.tcc_gr.dto.ModuleResponse;
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
    public List<ModuleResponse> getAllModules() {
        return moduleRepo.findAllByOrderByOrderIndexAsc()
                .stream().map(ModuleResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponse> getModule(@PathVariable Long id) {
        return moduleRepo.findById(id)
                .map(m -> ResponseEntity.ok(ModuleResponse.from(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ModuleResponse createModule(@RequestBody Module module) {
        return ModuleResponse.from(moduleRepo.save(module));
    }
}
