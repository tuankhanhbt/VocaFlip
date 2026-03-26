package com.example.vocaflip.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(min = 1, max = 100, message = "Full name must be between 1 and 100 characters")
    String fullName,

    @Min(value = 1, message = "Daily goal must be at least 1")
    @Max(value = 500, message = "Daily goal must be at most 500")
    Integer dailyGoal
) {
}
