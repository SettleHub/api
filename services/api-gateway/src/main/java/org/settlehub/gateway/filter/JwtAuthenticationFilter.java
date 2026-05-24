package org.settlehub.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;

/**
 * Custom Spring Cloud Gateway filter for JWT authentication.
 * Intercepts incoming requests, validates the Bearer token, and propagates the authenticated user context downstream.
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt-service.secret-key}")
    private String jwtSecretKey;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    /**
     * Applies the JWT authentication logic to the configured gateway route.
     * Validates the Authorization header and mutates the request to include the username.
     *
     * @param config the configuration properties for this filter
     * @return a {@link GatewayFilter} that processes the request
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (request.getHeaders().get(HttpHeaders.AUTHORIZATION) == null || request.getHeaders().get(HttpHeaders.AUTHORIZATION).isEmpty()) {
                logger.warn("Authorization header missing for request: {}", request.getURI());
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header format for request: {}", request.getURI());
                return onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                request = exchange.getRequest()
                        .mutate()
                        .header("X-auth-username", claims.getSubject())
                        .build();

            } catch (Exception e) {
                logger.error("JWT validation failed for token: {} - Error: {}", token, e.getMessage());
                return onError(exchange, "JWT Token validation failed", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    /**
     * Terminates the request chain and returns an HTTP error response.
     *
     * @param exchange      the current server web exchange
     * @param err           the error message to log
     * @param httpStatus the HTTP status code to return
     * @return a completed {@link Mono} representing the error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    /**
     * Decodes the Base64 encoded secret key and generates an HMAC SHA key.
     *
     * @return the {@link SecretKey} used for JWT signature validation
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Configuration class for {@link JwtAuthenticationFilter}.
     * Intended for route-specific parameters binding from application.yml.
     */
    public static class Config { }
    
}
