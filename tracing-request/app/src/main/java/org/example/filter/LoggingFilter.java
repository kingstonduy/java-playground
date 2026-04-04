package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Let request/response be processed first
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Now we can log safely
            logRequest(wrappedRequest);
            logResponse(wrappedResponse);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response) throws IOException {
        String body = "";
        byte[] buf = response.getContentAsByteArray();
        if (buf.length > 0) {
            body = new String(buf, StandardCharsets.UTF_8);
        }
        log.info("RESPONSE={}", body);
        response.copyBodyToResponse(); // important
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String body = "";
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            body = new String(buf, StandardCharsets.UTF_8);
        }

        log.info("REQUEST={}", body);
    }
}
