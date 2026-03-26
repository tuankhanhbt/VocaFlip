package com.example.vocaflip.auth.dto.response;

import com.example.vocaflip.user.dto.response.UserResponse;

public record AuthResponse(
    String accessToken,
    String tokenType,
    UserResponse user
) {
}
