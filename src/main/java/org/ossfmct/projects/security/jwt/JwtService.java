package org.ossfmct.projects.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for generating, validating and parsing JWT tokens.
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties("jwt-service")
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    /**
     * JWT Secret key for signing tokens.
     */
    @Value("${jwt-service.secret-key}")
    public String SECRET_KEY;

    /**
     * Token expiration time in ms.
     */
    @Value("${jwt-service.expiration-time-ms}")
    private long EXPIRATION_TIME;

    /**
     * Generate a JWT Token with Secret Key signing.
     * @param authentication using user credentials.
     * @return generated JWT token.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return Jwts.builder()
            .setSubject((userPrincipal.getUsername()))
            .claim("roles", roles)
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + EXPIRATION_TIME))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Generates a cryptographic key for HMAC-SHA signing using the JWT secret key.
     * @return a {@link Key} instance derived from the base64-decoded secret key.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    /**
     * Parse a username from token body.
     * @param token a JWT token in its own person.
     * @return parsed username string.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(key()).build()
            .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Parse a user roles from token claims.
     * @param token a JWT token in its own person.
     * @return Collection of user roles.
     */
    public Set<String> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(key()).build().parseClaimsJws(token).getBody();
        return claims.get("roles", Set.class);
    }

    /**
     * Validate a token using the signing key.
     * @param authToken a JWT token in its own person.
     * @return result of token validating.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Claims claims = Jwts.parser().setSigningKey(key()).build().parseClaimsJws(authToken).getBody();
            logger.info("JWT valid, roles: {}", claims.get("roles"));
            return true;
        } catch (Exception e) {
            logger.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }
}