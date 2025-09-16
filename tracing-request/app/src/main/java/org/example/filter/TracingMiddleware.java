//package org.example.filter;
//
//import com.github.f4b6a3.uuid.UuidCreator;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class TracingMiddleware extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // Generate new traceId if not provided
//        String traceId = request.getHeader("X-Trace-Id");
//        if (traceId == null || traceId.isEmpty()) {
//            traceId = UuidCreator.getTimeOrderedEpoch().toString();
//        }
//
//        // Set it on the response for correlation
//        response.setHeader("X-Trace-Id", traceId);
//
//        // Continue with the filter chain
//        filterChain.doFilter(request, response);
//    }
//}
