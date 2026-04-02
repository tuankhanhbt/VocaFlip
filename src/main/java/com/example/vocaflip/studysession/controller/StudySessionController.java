package com.example.vocaflip.studysession.controller;

import com.example.vocaflip.studysession.dto.FinishStudySessionResponse;
import com.example.vocaflip.studysession.dto.StartStudySessionResponse;
import com.example.vocaflip.studysession.dto.SubmitStudyCardRequest;
import com.example.vocaflip.studysession.service.StudySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping("/flashcard-sets/{setId}/start")
    public ResponseEntity<StartStudySessionResponse> startStudySession(
            @PathVariable Long setId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(studySessionService.startStudySession(setId, authentication));
    }

    @PostMapping("/{sessionId}/cards")
    public ResponseEntity<Void> submitStudyCard(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitStudyCardRequest request,
            Authentication authentication
    ) {
        studySessionService.submitStudyCard(sessionId, request, authentication);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{sessionId}/finish")
    public ResponseEntity<FinishStudySessionResponse> finishStudySession(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(studySessionService.finishStudySession(sessionId, authentication));
    }
}