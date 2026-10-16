package com.ufc.tcc_gr.controller;

import com.ufc.tcc_gr.dto.ProgressResponse;
import com.ufc.tcc_gr.security.AuthenticatedUser;
import com.ufc.tcc_gr.service.StudentProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class StudentProgressController {

    private final StudentProgressService progressService;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping("/user/{userId}")
    public List<ProgressResponse> getUserProgress(@PathVariable Long userId) {
        authenticatedUser.assertOwnership(userId);
        return progressService.getProgressByUser(userId)
                .stream().map(ProgressResponse::from).toList();
    }
}
