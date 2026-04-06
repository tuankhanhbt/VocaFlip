package com.example.vocaflip.flashcardset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlashcardSetRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    private String description;

    @Size(max = 30, message = "Source language must be at most 30 characters")
    private String sourceLanguage;

    @Size(max = 30, message = "Target language must be at most 30 characters")
    private String targetLanguage;
}
