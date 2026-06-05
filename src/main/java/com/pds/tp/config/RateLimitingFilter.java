package com.pds.tp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS_PER_WINDOW = 120;
    private static final long WINDOW_SECONDS = 60L;

    private final Map<String, RequestWindow> requestWindows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = resolveClientKey(request);
        RequestWindow window = requestWindows.computeIfAbsent(key, ignored -> new RequestWindow());

        synchronized (window) {
            long now = Instant.now().getEpochSecond();
            if (now - window.windowStart >= WINDOW_SECONDS) {
                window.windowStart = now;
                window.counter = 0;
            }

            window.counter++;
            if (window.counter > MAX_REQUESTS_PER_WINDOW) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit excedido\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String username = request.getHeader("X-User-Name");
        if (username != null && !username.isBlank()) {
            return "USER:" + username;
        }
        return "IP:" + request.getRemoteAddr();
    }

    private static final class RequestWindow {
        private long windowStart = Instant.now().getEpochSecond();
        private int counter = 0;
    }
}

