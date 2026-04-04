package com.example.vocaflip.studysession.service;

import com.example.vocaflip.flashcard.entity.Flashcard;
import com.example.vocaflip.flashcard.repository.FlashcardRepository;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.flashcardset.repository.FlashcardSetRepository;
import com.example.vocaflip.studysession.dto.StudyModeResponse;
import com.example.vocaflip.studysession.dto.StudyModeResultResponse;
import com.example.vocaflip.studysession.dto.StudyQuestionResponse;
import com.example.vocaflip.studysession.dto.SubmitStudyModeAnswerRequest;
import com.example.vocaflip.studysession.dto.SubmitStudyModeRequest;
import com.example.vocaflip.studysession.entity.ReviewRating;
import com.example.vocaflip.studysession.entity.StudyMode;
import com.example.vocaflip.studysession.entity.StudySession;
import com.example.vocaflip.studysession.entity.StudySessionCard;
import com.example.vocaflip.studysession.entity.StudySessionStatus;
import com.example.vocaflip.studysession.repository.StudySessionCardRepository;
import com.example.vocaflip.studysession.repository.StudySessionRepository;
import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyModeService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final StudySessionRepository studySessionRepository;
    private final StudySessionCardRepository studySessionCardRepository;

    @Transactional(readOnly = true)
    public StudyModeResponse generateStudyMode(Long setId, Authentication authentication) {
        User user = getCurrentUser(authentication);

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Flashcard set not found"));

        validateOwnership(flashcardSet, user);

        List<Flashcard> allCards = flashcardRepository.findByFlashcardSetIdOrderByOrderIndexAsc(setId);

        List<Flashcard> validQuestionCards = allCards.stream()
                .filter(this::hasValidFrontContent)
                .toList();

        if (validQuestionCards.size() < 4) {
            throw new RuntimeException("Study mode requires at least 4 valid cards in the deck");
        }

        List<Flashcard> shuffledCards = new ArrayList<>(validQuestionCards);
        Collections.shuffle(shuffledCards);

        int totalQuestions = getRandomQuestionCount(validQuestionCards.size());
        List<Flashcard> questionCards = shuffledCards.subList(0, totalQuestions);

        List<StudyQuestionResponse> questions = new ArrayList<>();

        for (Flashcard correctCard : questionCards) {
            questions.add(buildQuestion(correctCard, allCards));
        }

        return StudyModeResponse.builder()
                .setId(flashcardSet.getId())
                .setTitle(flashcardSet.getTitle())
                .totalQuestions(questions.size())
                .questions(questions)
                .build();
    }

    public StudyModeResultResponse submitStudyMode(
            Long setId,
            SubmitStudyModeRequest request,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Flashcard set not found"));

        validateOwnership(flashcardSet, user);

        List<Flashcard> allCards = flashcardRepository.findByFlashcardSetIdOrderByOrderIndexAsc(setId);

        if (allCards.isEmpty()) {
            throw new RuntimeException("This flashcard set has no cards");
        }

        Map<Long, Flashcard> flashcardMap = allCards.stream()
                .collect(Collectors.toMap(Flashcard::getId, Function.identity()));

        int totalQuestions = request.getAnswers().size();
        int correctAnswers = 0;

        StudySession studySession = new StudySession();
        studySession.setUser(user);
        studySession.setFlashcardSet(flashcardSet);
        studySession.setMode(StudyMode.STUDY);
        studySession.setStatus(StudySessionStatus.COMPLETED);
        studySession.setStartedAt(LocalDateTime.now());
        studySession.setEndedAt(LocalDateTime.now());
        studySession.setTotalCards(totalQuestions);
        studySession.setReviewedCards(totalQuestions);
        studySession.setAgainCount(0);
        studySession.setHardCount(0);
        studySession.setGoodCount(0);
        studySession.setEasyCount(0);

        StudySession savedSession = studySessionRepository.save(studySession);

        for (SubmitStudyModeAnswerRequest answerRequest : request.getAnswers()) {
            Flashcard questionCard = flashcardMap.get(answerRequest.getFlashcardId());

            if (questionCard == null) {
                throw new RuntimeException(
                        "Question flashcard not found in this deck: " + answerRequest.getFlashcardId()
                );
            }

            String correctAnswer = questionCard.getBackText();
            String selectedAnswer = answerRequest.getSelectedAnswer() == null
                    ? null
                    : answerRequest.getSelectedAnswer().trim();

            boolean isCorrect = correctAnswer != null
                    && selectedAnswer != null
                    && correctAnswer.equalsIgnoreCase(selectedAnswer);

            if (isCorrect) {
                correctAnswers++;
            }

            StudySessionCard sessionCard = new StudySessionCard();
            sessionCard.setStudySession(savedSession);
            sessionCard.setFlashcard(questionCard);
            sessionCard.setOrderIndex(answerRequest.getOrderIndex());
            sessionCard.setSelectedAnswer(selectedAnswer);
            sessionCard.setCorrectAnswer(correctAnswer);
            sessionCard.setCorrect(isCorrect);
            sessionCard.setRating(isCorrect ? ReviewRating.EASY : ReviewRating.HARD);
            sessionCard.setReviewedAt(LocalDateTime.now());
            
            studySessionCardRepository.save(sessionCard);
        }

        int wrongAnswers = totalQuestions - correctAnswers;
        double score = totalQuestions == 0 ? 0.0 : (correctAnswers * 100.0) / totalQuestions;

        // tận dụng field cũ
        savedSession.setGoodCount(correctAnswers);
        savedSession.setAgainCount(wrongAnswers);
        studySessionRepository.save(savedSession);

        return StudyModeResultResponse.builder()
                .sessionId(savedSession.getId())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .wrongAnswers(wrongAnswers)
                .score(score)
                .build();
    }

    private StudyQuestionResponse buildQuestion(Flashcard correctCard, List<Flashcard> allCards) {
        List<String> options = new ArrayList<>();
        options.add(correctCard.getBackText());

        List<Flashcard> distractors = allCards.stream()
                .filter(card -> !card.getId().equals(correctCard.getId()))
                .filter(card -> card.getBackText() != null && !card.getBackText().isBlank())
                .toList();

        List<Flashcard> shuffledDistractors = new ArrayList<>(distractors);
        Collections.shuffle(shuffledDistractors);

        for (Flashcard distractor : shuffledDistractors) {
            if (options.size() == 4) {
                break;
            }

            if (!options.contains(distractor.getBackText())) {
                options.add(distractor.getBackText());
            }
        }

        if (options.size() < 4) {
            throw new RuntimeException("Not enough unique answers to generate study mode options");
        }

        Collections.shuffle(options);

        return StudyQuestionResponse.builder()
                .flashcardId(correctCard.getId())
                .frontContentType(correctCard.getFrontContentType().name())
                .frontText(correctCard.getFrontText())
                .frontImageUrl(correctCard.getFrontImageUrl())
                .options(options)
                .build();
    }

    private boolean hasValidFrontContent(Flashcard flashcard) {
        if (flashcard.getFrontContentType() == null) {
            return false;
        }

        return switch (flashcard.getFrontContentType()) {
            case TEXT -> flashcard.getFrontText() != null && !flashcard.getFrontText().isBlank();
            case IMAGE -> flashcard.getFrontImageUrl() != null && !flashcard.getFrontImageUrl().isBlank();
        };
    }

    private int getRandomQuestionCount(int totalCards) {
        if (totalCards == 4) {
        return 4;
    }
    return ThreadLocalRandom.current().nextInt(4, totalCards + 1);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateOwnership(FlashcardSet flashcardSet, User user) {
        if (!flashcardSet.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to access this flashcard set");
        }
    }
}