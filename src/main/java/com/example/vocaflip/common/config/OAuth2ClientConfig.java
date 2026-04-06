package com.example.vocaflip.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.util.StringUtils;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
        @Value("${app.oauth2.google.client-id:}") String clientId,
        @Value("${app.oauth2.google.client-secret:}") String clientSecret
    ) {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            return registrationId -> null;
        }

        ClientRegistration googleRegistration = CommonOAuth2Provider.GOOGLE
            .getBuilder("google")
            .clientId(clientId)
            .clientSecret(clientSecret)
            .scope("openid", "profile", "email")
            .build();

        return new InMemoryClientRegistrationRepository(googleRegistration);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
        ClientRegistrationRepository clientRegistrationRepository
    ) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
}
