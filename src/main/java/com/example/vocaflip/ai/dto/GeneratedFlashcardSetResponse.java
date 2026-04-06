package com.example.vocaflip.ai.dto;

import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneratedFlashcardSetResponse {

    private Long flashcardSetId;
    private String title;
    private String description;
    private String topic;
    private String sourceLanguage;
    private String targetLanguage;
    private int requestedCount;
    private int actualCount;
    private List<FlashcardResponse> cards;
    private LocalDateTime createdAt;
}