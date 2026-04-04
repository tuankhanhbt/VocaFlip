package com.example.vocaflip.studysession.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionHistoryItemResponse {
    private Long sessionId;
    private Long setId;
    private String setTitle;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Double score;
}