package com.example.vocaflip.studysession.dto;

import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartStudySessionResponse {
    private Long sessionId;
    private Long setId;
    private String setTitle;
    private Integer totalCards;
    private List<FlashcardResponse> flashcards;
}