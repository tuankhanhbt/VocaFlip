package com.example.vocaflip.flashcardset.dto;

import java.time.LocalDateTime;

public record FlashcardSetResponse(
    Long id,
    String title,
    String description,
    String sourceLanguage,
    String targetLanguage,
    Boolean archived,
    Long cardCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String visibility,
    String shareCode,
    Boolean allowCopy,
    Boolean allowReview
) {
}
