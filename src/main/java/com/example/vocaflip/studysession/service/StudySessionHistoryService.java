package com.example.vocaflip.studysession.service;

import com.example.vocaflip.studysession.dto.StudySessionHistoryItemResponse;
import com.example.vocaflip.studysession.entity.StudySession;
import com.example.vocaflip.studysession.repository.StudySessionRepository;
import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudySessionHistoryService {

    private final StudySessionRepository studySessionRepository;
    private final UserRepository userRepository;

    public List<StudySessionHistoryItemResponse> getMyStudySessionHistory(Authentication authentication) {
        User user = getCurrentUser(authentication);

        List<StudySession> sessions = studySessionRepository.findByUserIdOrderByStartedAtDesc(user.getId());

        return sessions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private StudySessionHistoryItemResponse mapToResponse(StudySession session) {
        int totalQuestions = session.getTotalCards() == null ? 0 : session.getTotalCards();
        int correctAnswers = session.getGoodCount() == null ? 0 : session.getGoodCount();
        int wrongAnswers = session.getAgainCount() == null ? 0 : session.getAgainCount();
        double score = totalQuestions == 0 ? 0.0 : (correctAnswers * 100.0) / totalQuestions;

        return StudySessionHistoryItemResponse.builder()
                .sessionId(session.getId())
                .setId(session.getFlashcardSet().getId())
                .setTitle(session.getFlashcardSet().getTitle())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .wrongAnswers(wrongAnswers)
                .score(score)
                .build();
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}