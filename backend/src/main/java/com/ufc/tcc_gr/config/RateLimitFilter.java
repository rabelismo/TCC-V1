package com.ufc.tcc_gr.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_WINDOW = 30;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, RateWindow> windows = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String path = request.getRequestURI();

        if (!path.startsWith("/api/execute") && !path.startsWith("/api/submit")) {
            chain.doFilter(req, res);
            return;
        }

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String clientIp = getClientIp(request);
        RateWindow window = windows.compute(clientIp, (key, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new RateWindow(now, new AtomicInteger(0));
            }
            return existing;
        });

        if (window.counter.incrementAndGet() > MAX_REQUESTS_PER_WINDOW) {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Limite de requisições excedido. Aguarde 1 minuto.\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record RateWindow(long windowStart, AtomicInteger counter) {}
}
