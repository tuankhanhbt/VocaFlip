package com.example.vocaflip.studysession.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyModeResponse {
    private Long setId;
    private String setTitle;
    private Integer totalQuestions;
    private List<StudyQuestionResponse> questions;
}