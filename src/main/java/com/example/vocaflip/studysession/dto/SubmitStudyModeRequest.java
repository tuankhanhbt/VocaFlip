package com.example.vocaflip.studysession.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class SubmitStudyModeRequest {

    @Valid
    @NotEmpty
    private List<SubmitStudyModeAnswerRequest> answers;
}

