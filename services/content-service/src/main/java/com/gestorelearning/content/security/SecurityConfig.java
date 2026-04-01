package com.gestorelearning.content.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value())
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/health", "/info").permitAll()
                        // Lectura de cursos: ADMIN, TEACHER y STUDENT pueden ver
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        // Escritura de cursos: Solo ADMIN y TEACHER pueden modificar
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/**").hasAnyRole("ADMIN", "TEACHER")
                        // Escritura de MODULOS y UNIDADES: También ADMIN y TEACHER
                        .requestMatchers("/api/v1/modules/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/api/v1/units/**").hasAnyRole("ADMIN", "TEACHER")
                        // Ping: Cualquier usuario autenticado
                        .requestMatchers("/api/v1/ping").authenticated()
                        // Cerrar por defecto: cualquier otra ruta requiere estar logueado
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
