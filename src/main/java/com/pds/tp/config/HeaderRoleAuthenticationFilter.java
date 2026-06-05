package com.pds.tp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class HeaderRoleAuthenticationFilter extends OncePerRequestFilter {
    private static final String ROLE_HEADER = "X-User-Role";
    private static final String USER_HEADER = "X-User-Name";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String role = request.getHeader(ROLE_HEADER);
        String username = request.getHeader(USER_HEADER);

        if (role != null && !role.isBlank() && username != null && !username.isBlank()) {
            String normalizedRole = role.trim().toUpperCase();
            var authority = new SimpleGrantedAuthority("ROLE_" + normalizedRole);
            var authentication = new UsernamePasswordAuthenticationToken(username, "N/A", List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

