package com.example.vocaflip.flashcard.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlashcardResponse {

    private Long id;
    private Long flashcardSetId;
    private String frontContentType;
    private String frontText;
    private String frontImageUrl;
    private String backText;
    private String exampleText;
    private String noteText;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String phonetic;
    private String audioUrl;
}
