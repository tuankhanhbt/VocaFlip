package com.example.vocaflip.studysession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinishStudySessionResponse {
    private Long sessionId;
    private Integer totalCards;
    private Integer reviewedCards;
    private Integer againCount;
    private Integer hardCount;
    private Integer goodCount;
    private Integer easyCount;
    private String status;
}