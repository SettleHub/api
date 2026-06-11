package org.settlehub.booking.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
/**
 * Security configuration class for SettleHub Booking Service.
 * Configures endpoint authorization rules, disables CSRF protection,
 * and sets up the application as an OAuth2 Resource Server checking JWT tokens.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt-service.secret-key}")
    private String secretKey;

    /**
     * Configures the main security filter chain.
     * Defines public routes, protected administrative endpoints,
     * and default authentication mechanisms for microservice ecosystem.
     *
     * @param http the HttpSecurity configuration object
     * @return the configured SecurityFilterChain instance
     * @throws Exception if an error occurs during building the security context
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/health/**").permitAll()
                .requestMatchers("/reservations/availability").permitAll()
                .requestMatchers("/reservations/book").hasAnyAuthority("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_USER", "ROLE_VISITOR")
                .requestMatchers("/reservations/calendar").hasAnyAuthority("ROLE_ADMIN", "ROLE_MODERATOR")
                .requestMatchers("/management/categories").hasAnyAuthority("ROLE_ADMIN")
                .requestMatchers("/management/rooms").hasAnyAuthority("ROLE_ADMIN")
                .requestMatchers("/management/housekeeping-rules").hasAnyAuthority("ROLE_ADMIN")
                .requestMatchers("/housekeeping/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_USER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }

    /**
     * Creates a JwtDecoder bean for symmetric token validation.
     * By using the same JWT_SECRET_KEY as the IAM service, this microservice
     * can validate tokens instantly offline without making network calls.
     *
     * @return the configured JwtDecoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(keySpec).build();
    }

}
