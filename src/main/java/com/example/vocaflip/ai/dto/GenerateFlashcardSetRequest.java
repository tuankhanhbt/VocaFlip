package com.example.vocaflip.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateFlashcardSetRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    private String description;

    @NotBlank(message = "Topic is required")
    @Size(max = 150, message = "Topic must be at most 150 characters")
    private String topic;

    @Min(value = 4, message = "Count must be at least 4")
    @Max(value = 50, message = "Count must be at most 50")
    private int count;

    @Size(max = 30, message = "Source language must be at most 30 characters")
    private String sourceLanguage = "en";

    @Size(max = 30, message = "Target language must be at most 30 characters")
    private String targetLanguage = "vi";
}