package com.example.vocaflip.flashcardset.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SharedFlashcardSetResponse {

    private Long id;
    private String title;
    private String description;
    private String sourceLanguage;
    private String targetLanguage;
    private Long cardCount;
    private Boolean allowCopy;
}
