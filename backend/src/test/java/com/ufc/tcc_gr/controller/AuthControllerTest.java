package com.ufc.tcc_gr.controller;

import com.ufc.tcc_gr.model.User;
import com.ufc.tcc_gr.repository.UserRepository;
import com.ufc.tcc_gr.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepo;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /api/auth/register - deve criar conta com sucesso")
    void register_success() throws Exception {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("hashed");

        User saved = new User("test@test.com", "Test User", "hashed");
        saved.setId(1L);
        when(userRepo.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(eq(1L), eq("test@test.com"), any())).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "Test User", "email": "test@test.com", "password": "123456"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("POST /api/auth/register - deve rejeitar email duplicado")
    void register_duplicate() throws Exception {
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "Test", "email": "test@test.com", "password": "123456"}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login - deve autenticar com sucesso")
    void login_success() throws Exception {
        User user = new User("test@test.com", "Test", "hashed");
        user.setId(1L);
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);
        when(jwtService.generateToken(eq(1L), eq("test@test.com"), any())).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "test@test.com", "password": "123456"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /api/auth/login - deve rejeitar senha incorreta")
    void login_wrongPassword() throws Exception {
        User user = new User("test@test.com", "Test", "hashed");
        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong123", "hashed")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "test@test.com", "password": "wrong123"}
                            """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register - deve validar campos obrigatórios")
    void register_validation() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "", "email": "bad", "password": "12"}
                            """))
                .andExpect(status().isBadRequest());
    }
}
