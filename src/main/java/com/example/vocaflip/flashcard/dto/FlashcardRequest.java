package com.example.vocaflip.flashcard.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlashcardRequest {

    private String frontContentType;

    @Size(max = 500, message = "Front text must be at most 500 characters")
    private String frontText;

    @Size(max = 500, message = "Front image URL must be at most 500 characters")
    private String frontImageUrl;

    @NotBlank(message = "Back text is required")
    @Size(max = 500, message = "Back text must be at most 500 characters")
    private String backText;

    private String exampleText;

    private String noteText;

    private Integer orderIndex;

    @AssertTrue(message = "For TEXT cards, frontText is required. For IMAGE cards, frontImageUrl is required")
    public boolean isFrontContentValid() {
        String normalizedType = frontContentType == null ? "TEXT" : frontContentType.trim().toUpperCase();

        return switch (normalizedType) {
            case "TEXT" -> frontText != null && !frontText.trim().isBlank();
            case "IMAGE" -> frontImageUrl != null && !frontImageUrl.trim().isBlank();
            default -> false;
        };
    }
}
