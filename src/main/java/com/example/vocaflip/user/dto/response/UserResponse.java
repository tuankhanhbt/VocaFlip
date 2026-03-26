package com.example.vocaflip.user.dto.response;

import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.entity.UserRole;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String fullName,
    String email,
    UserRole role,
    Boolean active,
    Integer dailyGoal,
    Integer currentStreak,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getActive(),
            user.getDailyGoal(),
            user.getCurrentStreak(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
