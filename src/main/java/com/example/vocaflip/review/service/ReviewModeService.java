package com.example.vocaflip.review.service;

import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import com.example.vocaflip.flashcard.entity.Flashcard;
import com.example.vocaflip.flashcard.repository.FlashcardRepository;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.flashcardset.repository.FlashcardSetRepository;
import com.example.vocaflip.review.dto.ReviewModeResponse;
import com.example.vocaflip.review.entity.DeckReviewProgress;
import com.example.vocaflip.review.repository.DeckReviewProgressRepository;
import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewModeService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final DeckReviewProgressRepository deckReviewProgressRepository;

    public ReviewModeResponse getReviewMode(Long setId, Authentication authentication) {
        User user = getCurrentUser(authentication);

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Flashcard set not found"));

        validateOwnership(flashcardSet, user);

        List<Flashcard> cards = flashcardRepository.findByFlashcardSetIdOrderByOrderIndexAsc(setId);

        DeckReviewProgress progress = deckReviewProgressRepository
                .findByUserIdAndFlashcardSetId(user.getId(), setId)
                .orElseGet(() -> {
                    DeckReviewProgress newProgress = new DeckReviewProgress();
                    newProgress.setUser(user);
                    newProgress.setFlashcardSet(flashcardSet);
                    newProgress.setCurrentCardIndex(0);
                    newProgress.setLastReviewedAt(null);
                    return deckReviewProgressRepository.save(newProgress);
                });

        return ReviewModeResponse.builder()
                .setId(flashcardSet.getId())
                .setTitle(flashcardSet.getTitle())
                .currentCardIndex(progress.getCurrentCardIndex())
                .cards(cards.stream().map(this::mapToFlashcardResponse).toList())
                .build();
    }

    public void updateReviewProgress(Long setId, Integer currentCardIndex, Authentication authentication) {
        User user = getCurrentUser(authentication);

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Flashcard set not found"));

        validateOwnership(flashcardSet, user);

        List<Flashcard> cards = flashcardRepository.findByFlashcardSetIdOrderByOrderIndexAsc(setId);

        if (cards.isEmpty()) {
            throw new RuntimeException("This flashcard set has no cards");
        }

        if (currentCardIndex < 0 || currentCardIndex >= cards.size()) {
            throw new RuntimeException("Current card index is out of range");
        }

        DeckReviewProgress progress = deckReviewProgressRepository
                .findByUserIdAndFlashcardSetId(user.getId(), setId)
                .orElseGet(() -> {
                    DeckReviewProgress newProgress = new DeckReviewProgress();
                    newProgress.setUser(user);
                    newProgress.setFlashcardSet(flashcardSet);
                    newProgress.setCurrentCardIndex(0);
                    return newProgress;
                });

        progress.setCurrentCardIndex(currentCardIndex);
        progress.setLastReviewedAt(LocalDateTime.now());

        deckReviewProgressRepository.save(progress);
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