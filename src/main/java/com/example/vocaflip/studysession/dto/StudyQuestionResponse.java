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
public class StudyQuestionResponse {
    private Long flashcardId;
    private String frontContentType;
    private String frontText;
    private String frontImageUrl;
    private List<String> options;
}