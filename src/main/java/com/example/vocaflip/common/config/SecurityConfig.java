package com.example.vocaflip.common.config;

import com.example.vocaflip.common.security.JwtAuthenticationFilter;
import com.example.vocaflip.common.security.OAuth2AuthenticationFailureHandler;
import com.example.vocaflip.common.security.OAuth2AuthenticationSuccessHandler;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${app.oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth2.google.client-secret:}")
    private String googleClientSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSecurity security = http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/shared/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (isGoogleOauthEnabled()) {
            security.oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2AuthenticationSuccessHandler)
                .failureHandler(oauth2AuthenticationFailureHandler)
            );
        }

        return security.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
            Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList()
        );
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private boolean isGoogleOauthEnabled() {
        return googleClientId != null
            && !googleClientId.isBlank()
            && googleClientSecret != null
            && !googleClientSecret.isBlank();
    }
}
