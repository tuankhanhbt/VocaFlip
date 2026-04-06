package com.example.vocaflip.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.oauth2.failure-path:/login}")
    private String failurePath;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
            .path(normalizePath(failurePath))
            .queryParam("error", URLEncoder.encode("google_oauth_failed", StandardCharsets.UTF_8))
            .build(true)
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/login";
        }

        return path.startsWith("/") ? path : "/" + path;
    }
}
