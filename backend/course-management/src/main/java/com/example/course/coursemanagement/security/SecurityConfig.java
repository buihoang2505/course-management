package com.example.course.coursemanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter          jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // ── Public (no token needed) ──────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/payment/vnpay-return").permitAll()  // VNPay redirect (no token)
                        .requestMatchers("/api/payment/vnpay-ipn").permitAll()     // VNPay IPN (server-to-server)
                        .requestMatchers("/api/certificates/verify/**").permitAll()
                        .requestMatchers("/api/test/email/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/leaderboard/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()

                        // ── ADMIN only ────────────────────────────────────────────
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/notifications/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/*/role").hasRole("ADMIN")
                        .requestMatchers("/api/users/*/ban").hasRole("ADMIN")
                        .requestMatchers("/api/users/*/unban").hasRole("ADMIN")
                        .requestMatchers("/api/users/*/downgrade").hasRole("ADMIN")
                        .requestMatchers("/api/grades/assign").hasRole("ADMIN")
                        .requestMatchers("/api/grades/calculate/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/quizzes").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT,    "/api/quizzes/**").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST,   "/api/quizzes/*/questions").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT,    "/api/quizzes/questions/**").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/questions/**").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers("/api/quizzes/admin/**").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers("/api/quizzes/*/admin/detail").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST,   "/api/courses").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT,    "/api/courses/**").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasAnyRole("ADMIN","INSTRUCTOR")
                        .requestMatchers("/api/certificates/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/payment/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/instructor-applications/**").hasRole("ADMIN")

                        // ── INSTRUCTOR ────────────────────────────────────────────
                        .requestMatchers("/api/instructor/**").hasAnyRole("ADMIN","INSTRUCTOR")

                        // ── Authenticated (any logged-in user) ────────────────────
                        .requestMatchers("/api/instructor-applications/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}