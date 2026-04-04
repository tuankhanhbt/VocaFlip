package com.example.vocaflip.review.dto;

import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewModeResponse {
    private Long setId;
    private String setTitle;
    private Integer currentCardIndex;
    private List<FlashcardResponse> cards;
}