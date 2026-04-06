package com.example.vocaflip.common.security;

import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.oauth2.success-path:/oauth2/callback}")
    private String successPath;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Authentication.getPrincipal();

        String email = normalizeEmail((String) oauth2User.getAttributes().get("email"));
        if (!StringUtils.hasText(email)) {
            throw new ServletException("Google account did not return an email");
        }

        String fullName = defaultFullName(
            (String) oauth2User.getAttributes().get("name"),
            email
        );

        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createGoogleUser(email, fullName));

        if (!StringUtils.hasText(user.getFullName())) {
            user.setFullName(fullName);
            user = userRepository.save(user);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
            .path(normalizePath(successPath))
            .queryParam("token", token)
            .queryParam("provider", oauth2Authentication.getAuthorizedClientRegistrationId())
            .build(true)
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User createGoogleUser(String email, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        return userRepository.save(user);
    }

    private String defaultFullName(String fullName, String email) {
        if (StringUtils.hasText(fullName)) {
            return fullName.trim();
        }

        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/oauth2/callback";
        }

        return path.startsWith("/") ? path : "/" + path;
    }
}
