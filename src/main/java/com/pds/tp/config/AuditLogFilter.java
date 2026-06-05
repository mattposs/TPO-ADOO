package com.pds.tp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audits state-changing HTTP requests for traceability requirements.
 */
@Slf4j
@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter AUDIT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String AUDIT_LOG_PREFIX = "[AUDIT]";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();
        String user = request.getUserPrincipal() != null
                ? request.getUserPrincipal().getName()
                : "ANONYMOUS";

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (isModifyingRequest(method)) {
                long duration = System.currentTimeMillis() - startTime;
                log.info("{} MODIFICATION | {} {} | User: {} | Status: {} | Duration: {}ms | Timestamp: {}",
                        AUDIT_LOG_PREFIX,
                        method,
                        path,
                        user,
                        response.getStatus(),
                        duration,
                        LocalDateTime.now().format(AUDIT_DATE_FORMATTER));
            }
        }
    }

    private boolean isModifyingRequest(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }
}


