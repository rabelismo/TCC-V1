package com.ufc.tcc_gr.security;

import com.ufc.tcc_gr.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "chave-de-teste-com-pelo-menos-256-bits-para-hmac-sha-256";

    private final JwtService jwt = new JwtService(SECRET, 60_000);

    @Test
    @DisplayName("Token válido devolve as claims")
    void verify_validToken() {
        String token = jwt.generateToken(7L, "a@b.c", UserRole.STUDENT);

        assertThat(jwt.verify(token)).hasValueSatisfying(claims -> {
            assertThat(claims.getSubject()).isEqualTo("7");
            assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
        });
    }

    @Test
    @DisplayName("Token expirado é rejeitado")
    void verify_expiredToken() {
        String token = new JwtService(SECRET, -1_000).generateToken(7L, "a@b.c", UserRole.STUDENT);

        assertThat(jwt.verify(token)).isEmpty();
    }

    @Test
    @DisplayName("Token assinado com outra chave é rejeitado")
    void verify_foreignSignature() {
        String token = new JwtService(SECRET + "-outra", 60_000).generateToken(7L, "a@b.c", UserRole.STUDENT);

        assertThat(jwt.verify(token)).isEmpty();
    }

    @Test
    @DisplayName("Texto que não é JWT é rejeitado")
    void verify_garbage() {
        assertThat(jwt.verify("nao.e.jwt")).isEmpty();
        assertThat(jwt.verify("")).isEmpty();
    }
}
