package com.example.vocaflip.studysession.service;

import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import com.example.vocaflip.flashcard.entity.Flashcard;
import com.example.vocaflip.flashcard.repository.FlashcardRepository;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.flashcardset.repository.FlashcardSetRepository;
import com.example.vocaflip.progress.entity.CardProgressStatus;
import com.example.vocaflip.progress.entity.UserCardProgress;
import com.example.vocaflip.progress.repository.UserCardProgressRepository;
import com.example.vocaflip.studysession.dto.FinishStudySessionResponse;
import com.example.vocaflip.studysession.dto.StartStudySessionResponse;
import com.example.vocaflip.studysession.dto.SubmitStudyCardRequest;
import com.example.vocaflip.studysession.entity.ReviewRating;
import com.example.vocaflip.studysession.entity.StudyMode;
import com.example.vocaflip.studysession.entity.StudySession;
import com.example.vocaflip.studysession.entity.StudySessionCard;
import com.example.vocaflip.studysession.entity.StudySessionStatus;
import com.example.vocaflip.studysession.repository.StudySessionCardRepository;
import com.example.vocaflip.studysession.repository.StudySessionRepository;
import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final StudySessionCardRepository studySessionCardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final UserCardProgressRepository userCardProgressRepository;

    public StartStudySessionResponse startStudySession(Long setId, Authentication authentication) {
        User user = getCurrentUser(authentication);

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Flashcard set not found"));

        validateFlashcardSetOwnership(flashcardSet, user);

        List<Flashcard> flashcards = flashcardRepository.findByFlashcardSetIdOrderByOrderIndexAsc(setId);

        StudySession studySession = new StudySession();
        studySession.setUser(user);
        studySession.setFlashcardSet(flashcardSet);
        studySession.setMode(StudyMode.STUDY);
        studySession.setStatus(StudySessionStatus.IN_PROGRESS);
        studySession.setStartedAt(LocalDateTime.now());
        studySession.setTotalCards(flashcards.size());
        studySession.setReviewedCards(0);
        studySession.setAgainCount(0);
        studySession.setHardCount(0);
        studySession.setGoodCount(0);
        studySession.setEasyCount(0);

        StudySession savedSession = studySessionRepository.save(studySession);

        List<FlashcardResponse> flashcardResponses = flashcards.stream()
                .map(this::mapToFlashcardResponse)
                .toList();

        return StartStudySessionResponse.builder()
                .sessionId(savedSession.getId())
                .setId(flashcardSet.getId())
                .setTitle(flashcardSet.getTitle())
                .totalCards(flashcards.size())
                .flashcards(flashcardResponses)
                .build();
    }

    public void submitStudyCard(Long sessionId, SubmitStudyCardRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);

        StudySession studySession = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Study session not found"));

        validateStudySessionOwnership(studySession, user);

        if (studySession.getStatus() != StudySessionStatus.IN_PROGRESS) {
            throw new RuntimeException("Study session is not in progress");
        }

        Flashcard flashcard = flashcardRepository.findById(request.getFlashcardId())
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));

        validateFlashcardBelongsToSet(flashcard, studySession.getFlashcardSet());

        StudySessionCard studySessionCard = new StudySessionCard();
        studySessionCard.setStudySession(studySession);
        studySessionCard.setFlashcard(flashcard);
        studySessionCard.setOrderIndex(request.getOrderIndex());
        studySessionCard.setRating(request.getRating());
        studySessionCard.setReviewedAt(LocalDateTime.now());

        studySessionCardRepository.save(studySessionCard);

        updateStudySessionStats(studySession, request.getRating());
        updateUserCardProgress(user, flashcard, request.getRating());
    }

    public FinishStudySessionResponse finishStudySession(Long sessionId, Authentication authentication) {
        User user = getCurrentUser(authentication);

        StudySession studySession = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Study session not found"));

        validateStudySessionOwnership(studySession, user);

        studySession.setStatus(StudySessionStatus.COMPLETED);
        studySession.setEndedAt(LocalDateTime.now());

        StudySession savedSession = studySessionRepository.save(studySession);

        return FinishStudySessionResponse.builder()
                .sessionId(savedSession.getId())
                .totalCards(savedSession.getTotalCards())
                .reviewedCards(savedSession.getReviewedCards())
                .againCount(savedSession.getAgainCount())
                .hardCount(savedSession.getHardCount())
                .goodCount(savedSession.getGoodCount())
                .easyCount(savedSession.getEasyCount())
                .status(savedSession.getStatus().name())
                .build();
    }

    private void updateStudySessionStats(StudySession studySession, ReviewRating rating) {
        studySession.setReviewedCards(studySession.getReviewedCards() + 1);

        switch (rating) {
            case AGAIN -> studySession.setAgainCount(studySession.getAgainCount() + 1);
            case HARD -> studySession.setHardCount(studySession.getHardCount() + 1);
            case GOOD -> studySession.setGoodCount(studySession.getGoodCount() + 1);
            case EASY -> studySession.setEasyCount(studySession.getEasyCount() + 1);
        }

        studySessionRepository.save(studySession);
    }

    private void updateUserCardProgress(User user, Flashcard flashcard, ReviewRating rating) {
        UserCardProgress progress = userCardProgressRepository
                .findByUserIdAndFlashcardId(user.getId(), flashcard.getId())
                .orElseGet(() -> {
                    UserCardProgress newProgress = new UserCardProgress();
                    newProgress.setUser(user);
                    newProgress.setFlashcard(flashcard);
                    newProgress.setStatus(CardProgressStatus.NEW);
                    newProgress.setReviewCount(0);
                    newProgress.setCorrectStreak(0);
                    newProgress.setIntervalDays(0);
                    newProgress.setEasinessFactor(BigDecimal.valueOf(2.50));
                    newProgress.setLapseCount(0);
                    return newProgress;
                });

        int intervalDays = calculateIntervalDays(rating);

        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setNextReviewAt(LocalDateTime.now().plusDays(intervalDays));
        progress.setIntervalDays(intervalDays);
        progress.setReviewCount(progress.getReviewCount() + 1);

        switch (rating) {
            case AGAIN -> {
                progress.setLapseCount(progress.getLapseCount() + 1);
                progress.setCorrectStreak(0);
                progress.setStatus(CardProgressStatus.LEARNING);
            }
            case HARD -> {
                progress.setStatus(CardProgressStatus.REVIEW_NEEDED);
            }
            case GOOD -> {
                progress.setCorrectStreak(progress.getCorrectStreak() + 1);
                progress.setStatus(CardProgressStatus.REVIEW_NEEDED);
            }
            case EASY -> {
                progress.setCorrectStreak(progress.getCorrectStreak() + 1);
                progress.setStatus(CardProgressStatus.MASTERED);
            }
        }

        userCardProgressRepository.save(progress);
    }

    private int calculateIntervalDays(ReviewRating rating) {
        return switch (rating) {
            case AGAIN -> 1;
            case HARD -> 2;
            case GOOD -> 4;
            case EASY -> 7;
        };
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateFlashcardSetOwnership(FlashcardSet flashcardSet, User user) {
        if (!flashcardSet.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to access this flashcard set");
        }
    }

    private void validateStudySessionOwnership(StudySession studySession, User user) {
        if (!studySession.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to access this study session");
        }
    }

    private void validateFlashcardBelongsToSet(Flashcard flashcard, FlashcardSet flashcardSet) {
        if (!flashcard.getFlashcardSet().getId().equals(flashcardSet.getId())) {
            throw new RuntimeException("Flashcard does not belong to this flashcard set");
        }
    }

    private FlashcardResponse mapToFlashcardResponse(Flashcard flashcard) {
        return FlashcardResponse.builder()
                .id(flashcard.getId())
                .frontText(flashcard.getFrontText())
                .frontImageUrl(flashcard.getFrontImageUrl())
                .backText(flashcard.getBackText())
                .exampleText(flashcard.getExampleText())
                .noteText(flashcard.getNoteText())
                .frontContentType(flashcard.getFrontContentType().name())
                .orderIndex(flashcard.getOrderIndex())
                .build();
    }
}