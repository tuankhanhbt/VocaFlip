package com.example.vocaflip.studysession.dto;

import com.example.vocaflip.studysession.entity.ReviewRating;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitStudyCardRequest {

    @NotNull
    private Long flashcardId;

    @NotNull
    private Integer orderIndex;

    @NotNull
    private ReviewRating rating;
}