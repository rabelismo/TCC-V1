package com.ufc.tcc_gr.controller;

import com.ufc.tcc_gr.dto.SubmissionRequest;
import com.ufc.tcc_gr.dto.SubmissionResult;
import com.ufc.tcc_gr.security.AuthenticatedUser;
import com.ufc.tcc_gr.security.JwtService;
import com.ufc.tcc_gr.service.StudentProgressService;
import com.ufc.tcc_gr.service.SubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SubmissionController.class, StudentProgressController.class})
@AutoConfigureMockMvc(addFilters = false)
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubmissionService submissionService;

    @MockitoBean
    private StudentProgressService progressService;

    @MockitoBean
    private AuthenticatedUser authenticatedUser;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /api/submit usa o usuário do token e ignora userId do corpo")
    void submit_usesAuthenticatedUser() throws Exception {
        when(authenticatedUser.getId()).thenReturn(7L);
        when(submissionService.evaluate(eq(7L), any(SubmissionRequest.class)))
                .thenReturn(SubmissionResult.builder()
                        .allPassed(true).passedCount(1).totalCount(1).criteria(List.of()).build());

        mockMvc.perform(post("/api/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"moduleId": 1, "userId": 999, "code": "print('oi')"}
                            """))
                .andExpect(status().isOk());

        verify(submissionService).evaluate(eq(7L), any(SubmissionRequest.class));
    }

    @Test
    @DisplayName("JSON malformado responde 400, não 500")
    void submit_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new byte[]{'{', '"', 'c', '"', ':', '"', 'O', 'l', (byte) 0xE1, ' ', '"', '}'}))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(submissionService);
    }

    @Test
    @DisplayName("POST /api/progress não existe mais: progresso não é escrito pelo cliente")
    void progress_cannotBeWrittenByClient() throws Exception {
        mockMvc.perform(post("/api/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"userId": 7, "moduleId": 1, "status": "COMPLETED"}
                            """))
                .andExpect(status().isNotFound());
        verifyNoInteractions(progressService);
    }
}
