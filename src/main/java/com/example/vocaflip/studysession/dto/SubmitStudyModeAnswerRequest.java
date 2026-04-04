package com.example.vocaflip.studysession.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitStudyModeAnswerRequest {

    @NotNull
    private Long flashcardId;

    @NotBlank
    private String selectedAnswer;

    @NotNull
    private Integer orderIndex;
}