package com.ufc.tcc_gr.controller;

import com.ufc.tcc_gr.dto.ProgressResponse;
import com.ufc.tcc_gr.dto.ProgressUpdateRequest;
import com.ufc.tcc_gr.service.StudentProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class StudentProgressController {

    private final StudentProgressService progressService;

    @GetMapping("/user/{userId}")
    public List<ProgressResponse> getUserProgress(@PathVariable Long userId) {
        return progressService.getProgressByUser(userId)
                .stream().map(ProgressResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<ProgressResponse> updateProgress(@Valid @RequestBody ProgressUpdateRequest request) {
        return ResponseEntity.ok(ProgressResponse.from(progressService.updateProgress(request)));
    }
}
