//package org.example;
//
//import lombok.RequiredArgsConstructor;
//import org.example.filter.ApiKeyAuthFilter;
//import org.example.filter.LoggingFilter;
//import org.example.filter.TracingMiddleware;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import java.util.List;
//
//@Configuration
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private static final List<String> WHITELIST = List.of(
//            "/actuator/**",
//            "/public/**"
//    );
//
//    private final ApiKeyAuthFilter apiKeyAuthFilter;
//    private final TracingMiddleware tracingMiddleware;
//    private final LoggingFilter loggingFilter;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .formLogin(form -> form.disable())
//                .httpBasic(basic -> basic.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(WHITELIST.toArray(new String[0]))
//                        .permitAll()
//                        .anyRequest().authenticated()
//                )
//                // order matters: tracing → apiKey → logging
//                .addFilterBefore(tracingMiddleware, UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .addFilterAfter(loggingFilter, UsernamePasswordAuthenticationFilter.class);
//
//        // add filter after proccessing and before sending response
//        return http.build();
//    }
//}
