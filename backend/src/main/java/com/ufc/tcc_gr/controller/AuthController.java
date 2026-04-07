package com.ufc.tcc_gr.controller;

import com.ufc.tcc_gr.dto.AuthRequest;
import com.ufc.tcc_gr.dto.AuthResponse;
import com.ufc.tcc_gr.dto.RegisterRequest;
import com.ufc.tcc_gr.model.User;
import com.ufc.tcc_gr.repository.UserRepository;
import com.ufc.tcc_gr.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email já cadastrado"));
        }

        User user = new User(
                request.getEmail(),
                request.getName(),
                passwordEncoder.encode(request.getPassword())
        );
        user = userRepo.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email ou senha inválidos"));
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(
                new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name()));
    }
}
