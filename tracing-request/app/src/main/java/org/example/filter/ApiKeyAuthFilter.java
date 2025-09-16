//package org.example.filter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//
//@Component
//public class ApiKeyAuthFilter extends OncePerRequestFilter {
//
//    private static final List<String> WHITELIST = List.of("/public/", "/actuator/");
//    private final String apiKey;
//
//    public ApiKeyAuthFilter(@Value("${security.api-key}") String apiKey) {
//        this.apiKey = apiKey;
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//        return WHITELIST.stream().anyMatch(path::startsWith);
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String providedKey = request.getHeader("x-api-key");
//
//        if (providedKey == null) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing x-api-key header");
//            return;
//        }
//
//        if (!apiKey.equals(providedKey)) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid x-api-key header");
//            return;
//        }
//
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken("api-key-user", null, List.of())
//        );
//
//        filterChain.doFilter(request, response);
//    }
//}
