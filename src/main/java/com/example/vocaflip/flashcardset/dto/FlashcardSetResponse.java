package com.example.vocaflip.flashcardset.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlashcardSetResponse {

    private Long id;
    private String title;
    private String description;
    private String sourceLanguage;
    private String targetLanguage;
    private Boolean archived;
    private Long cardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
