//package org.example.filter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.util.ContentCachingRequestWrapper;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//@Component
//@Slf4j
//public class LoggingFilter extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        // Wrap request to allow multiple reads
//        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
//
//        try {
//            filterChain.doFilter(wrappedRequest, response);
//        } finally {
//            logRequest(wrappedRequest, response);
//        }
//    }
//
//    private void logRequest(ContentCachingRequestWrapper request, HttpServletResponse response) {
//        String traceId = response.getHeader("X-Trace-Id");
//        String method = request.getMethod();
//        String uri = request.getRequestURI();
//
//        String body = "";
//        byte[] buf = request.getContentAsByteArray();
//        if (buf.length > 0) {
//            body = new String(buf, StandardCharsets.UTF_8);
//        }
//
//        log.info("[{}][{}][{}] - REQUEST_BODY={}", traceId, method, uri, body);
//    }
//}
