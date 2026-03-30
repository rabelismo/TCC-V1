package com.ufc.tcc_gr.controller;

import com.ufc.tcc_gr.dto.SubmissionRequest;
import com.ufc.tcc_gr.dto.SubmissionResult;
import com.ufc.tcc_gr.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submit")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<SubmissionResult> submit(@RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(submissionService.evaluate(request));
    }
}
