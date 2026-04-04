package com.example.vocaflip.review.repository;

import com.example.vocaflip.review.entity.DeckReviewProgress;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckReviewProgressRepository extends JpaRepository<DeckReviewProgress, Long> {

    Optional<DeckReviewProgress> findByUserIdAndFlashcardSetId(Long userId, Long flashcardSetId);
}