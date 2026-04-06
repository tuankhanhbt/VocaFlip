package com.example.vocaflip.studysession.controller;

import com.example.vocaflip.studysession.dto.StudyModeResponse;
import com.example.vocaflip.studysession.dto.StudyModeResultResponse;
import com.example.vocaflip.studysession.dto.SubmitStudyModeRequest;
import com.example.vocaflip.studysession.service.StudyModeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-mode")
@RequiredArgsConstructor
public class StudyModeController {

    private final StudyModeService studyModeService;

    @GetMapping("/flashcard-sets/{setId}")
    public ResponseEntity<StudyModeResponse> getStudyMode(
            @PathVariable Long setId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(studyModeService.generateStudyMode(setId, authentication));
    }

    @PostMapping("/flashcard-sets/{setId}/submit")
    public ResponseEntity<StudyModeResultResponse> submitStudyMode(
            @PathVariable Long setId,
            @Valid @RequestBody SubmitStudyModeRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(studyModeService.submitStudyMode(setId, request, authentication));
    }
}