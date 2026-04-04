package com.example.vocaflip.studysession.controller;

import com.example.vocaflip.studysession.dto.StudySessionHistoryItemResponse;
import com.example.vocaflip.studysession.service.StudySessionHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
public class StudySessionHistoryController {

    private final StudySessionHistoryService studySessionHistoryService;

    @GetMapping("/history")
    public ResponseEntity<List<StudySessionHistoryItemResponse>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(studySessionHistoryService.getMyStudySessionHistory(authentication));
    }
}