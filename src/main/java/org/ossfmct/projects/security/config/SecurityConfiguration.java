package org.ossfmct.projects.security.config;

import jakarta.servlet.http.HttpServletRequest;
import org.ossfmct.projects.security.jwt.AuthEntryPointJwt;
import org.ossfmct.projects.security.jwt.filters.JwtAuthFilter;
import org.ossfmct.projects.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * General Security configuration of application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * List contains ip addresses and domains that are used for development or will be used in production
     * <ul>
     *     <li><bold>protocol: http; ip: 127.0.0.1; port: 8080;</bold> - Client address on local machine.</li>
     *     <li><bold>protocol: https; ip: 127.0.0.1; port: 8080;</bold> - Client address on local machine with SSL encryption.</li>
     *     <li><bold>protocol: http; ip: 172.19.0.10; port: 8080;</bold> - Client address in docker network.</li>
     *     <li><bold>protocol: https; ip: 172.19.0.10; port: 8080;</bold> - Client address in docker network with SSL encryption.</li>
     *     <li><bold>protocol: http; domain: esettlement.knutd.edu.ua; port: 80;</bold> - Possible production domain without SSL encryption.</li>
     *     <li><bold>protocol: https; domain: esettlement.knutd.edu.ua; port: 443;</bold> - Possible production domain with SSL encryption.</li>
     *
     *     <li><bold>protocol: http; ip: 127.0.0.1; port: 8082;</bold> - Admin Panel address on local machine.</li>
     *     <li><bold>protocol: https; ip: 127.0.0.1; port: 8082;</bold> - Admin Panel address on local machine with SSL encryption.</li>
     *     <li><bold>protocol: http; ip: 172.19.0.11; port: 8082;</bold> - Admin Panel address in docker network.</li>
     *     <li><bold>protocol: https; ip: 172.19.0.11; port: 8082;</bold> - Admin Panel address in docker network with SSL encryption.</li>
     * </ul>
     */
    private static final List<String> ALLOWED_ORIGINS = List.of(
        "http://127.0.0.1:8080",
        "https://127.0.0.1:8080",
        "http://172.19.0.10:8080",
        "https://172.19.0.10:8080",
        "https://esettlement.knutd.edu.ua:80",
        "https://esettlement.knutd.edu.ua:443",
        "http://127.0.0.1:8082",
        "https://127.0.0.1:8082",
        "http://172.19.0.11:8082",
        "https://172.19.0.11:8082"
    );

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    /**
     * @return Vanilla CORS Configuration
     * CORS means Cross-Origin Resource Sharing.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    /**
     * Configures the security filter chain for the application.
     * This method sets up various security features such as:
     * <ul>
     *     <li>Disabling CSRF protection.</li>
     *     <li>Handling unauthorized access with a custom authentication entry point.</li>
     *     <li>Configuring CORS (Cross-Origin Resource Sharing) with specific allowed origins, methods, headers, and credentials.</li>
     *     <li>Defining URL authorization rules (e.g., permitting access to "/identity/**" and restricting access to other URLs to users with the "ROLE_ADMIN" authority).</li>
     *     <li>Enforcing stateless session management (useful for JWT-based authentication).</li>
     *     <li>Adding a custom JWT authentication filter.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} object used to configure web-based security for HTTP requests.
     * @param jwtAuthFilter the custom JWT authentication filter to be applied to incoming requests.
     * @return the configured {@link SecurityFilterChain} object that manages security filter settings.
     * @throws Exception if an error occurs during security configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .cors(corsCustomizer -> corsCustomizer.configurationSource(
                new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration corsConfiguration = new CorsConfiguration();
                        corsConfiguration.setAllowCredentials(true);
                        corsConfiguration.setAllowedOrigins(ALLOWED_ORIGINS);
                        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
                        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
                        corsConfiguration.setMaxAge(Duration.ofMinutes(5L));
                        return corsConfiguration;
                    }
                }
            ))
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/identity/signin").permitAll()
                .requestMatchers("/identity/whoami").permitAll()
                .requestMatchers("/identity/signup/student").permitAll()
                .requestMatchers("/identity/verify").permitAll()
                .requestMatchers("/identity/forgotPassword").permitAll()
                .requestMatchers("/identity/forgotPassword/verify").permitAll()
                .requestMatchers("/identity/forgotPassword/update").permitAll()
                .requestMatchers("/identity/signup").hasAnyAuthority("ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/hostels/save").hasAnyAuthority("ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/hostels/**").permitAll()
                .requestMatchers("/view/hostel/**").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/users/get").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/users/update-contacts").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/users/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/submissions/add").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/submissions/get/allBySubmitter").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/submissions/get/activeCount").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/submissions/get").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/submissions/delete").hasAnyAuthority("ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/submissions/file/add").hasAnyAuthority("ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/submissions/file/delete").hasAnyAuthority("ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/submissions/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers("/documents/load").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MODERATOR", "ROLE_ADMIN")
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            ))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * @return the {@link DaoAuthenticationProvider} object is an AuthenticationProvider implementation that retrieves user details from a UserDetailsService.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * @return the {@link AuthenticationManager} object is responsible for providing a robust and flexible way to manage authentication processes in your application.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Password Encoder needs for hashing users authorization phrases.
     * @return the {@link BCryptPasswordEncoder} object which a realization of {@link PasswordEncoder} interface.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * @return the {@link UserDetailsServiceImpl} object is implementation of UserDetailsService interface which a core component used for loading user-specific data.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }
}
