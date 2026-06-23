package com.ufc.tcc_gr.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {

    public Long getId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Nenhum usuário autenticado encontrado.");
        }
        return (Long) auth.getPrincipal();
    }

    public void assertOwnership(Long requestUserId) {
        Long authenticatedId = getId();
        if (!authenticatedId.equals(requestUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Acesso negado: você não tem permissão para acessar recursos de outro usuário.");
        }
    }
}
