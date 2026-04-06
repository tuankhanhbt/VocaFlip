package com.example.vocaflip.flashcardset.dto;


public record SharedFlashcardSetResponse(
    Long id,
    String title,
    String description,
    String sourceLanguage,
    String targetLanguage,
    Long cardCount,
    Boolean allowCopy,
    Boolean allowReview
) {
}
