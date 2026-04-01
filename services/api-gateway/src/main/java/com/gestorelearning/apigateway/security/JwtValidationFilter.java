package com.gestorelearning.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/health",
            "/info",
            "/api/v1/ping",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/organizations",
            "/auth/api/v1/auth/login",
            "/auth/api/v1/auth/register",
            "/auth/api/v1/auth/organizations"
    );

    private final JwtService jwtService;

    public JwtValidationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod()) || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Busqueda resiliente de la cabecera Authorization (case-insensitive)
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            // Reintento manual por si el cliente la mando en minusculas de forma no estandar
            authHeader = request.getHeaders().getFirst("authorization");
        }

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        // Extraer token (soporta multiples espacios por error del cliente)
        String token = authHeader.substring(7).trim();

        try {
            Claims claims = jwtService.parseClaims(token);
            String role = claims.get("role", String.class);
            String organizationId = claims.get("organizationId", String.class);

            if (role == null || role.isBlank() || organizationId == null || organizationId.isBlank()) {
                return unauthorized(exchange, "Missing security claims");
            }

            // Propagar Claims en cabeceras X- para facilitar la vida a los microservicios
            // pero MANTENIENDO la cabecera Authorization original para validacion Zero-Trust
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Role", role)
                    .header("X-Organization-Id", organizationId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException ex) {
            return unauthorized(exchange, "Invalid JWT");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> path.equals(p) || path.endsWith(p));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] responseBody = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBody);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
