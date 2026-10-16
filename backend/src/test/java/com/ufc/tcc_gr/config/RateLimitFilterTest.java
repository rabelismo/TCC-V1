package com.ufc.tcc_gr.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private int submit(Long userId, String forwardedFor) throws Exception {
        if (userId != null) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of()));
        } else {
            SecurityContextHolder.clearContext();
        }
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/submit");
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    @DisplayName("Limite é por usuário e não muda com X-Forwarded-For forjado")
    void limit_isPerUser_andIgnoresForwardedFor() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_REQUESTS_PER_WINDOW; i++) {
            assertThat(submit(7L, "10.0.0." + i)).isEqualTo(200);
        }

        assertThat(submit(7L, "203.0.113.99")).isEqualTo(429);
        assertThat(submit(8L, null)).isEqualTo(200);
    }

    @Test
    @DisplayName("Resposta 429 é JSON em UTF-8")
    void rejection_isUtf8Json() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_REQUESTS_PER_WINDOW; i++) {
            submit(9L, null);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(9L, null, List.of()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST", "/api/execute"), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getCharacterEncoding()).isEqualToIgnoringCase("UTF-8");
        assertThat(new String(response.getContentAsByteArray(), java.nio.charset.StandardCharsets.UTF_8))
                .contains("requisições");
    }

    @Test
    @DisplayName("Sem usuário, usa o endereço remoto e não o cabeçalho")
    void anonymous_usesRemoteAddress() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_REQUESTS_PER_WINDOW; i++) {
            submit(null, "198.51.100." + i);
        }

        assertThat(submit(null, "198.51.100.250")).isEqualTo(429);
        assertThat(filter.trackedClients()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rotas fora de execute/submit não são limitadas")
    void otherRoutes_areNotLimited() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        for (int i = 0; i <= RateLimitFilter.MAX_REQUESTS_PER_WINDOW; i++) {
            response = new MockHttpServletResponse();
            filter.doFilter(new MockHttpServletRequest("GET", "/api/modules"), response, new MockFilterChain());
        }
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(filter.trackedClients()).isZero();
    }
}
