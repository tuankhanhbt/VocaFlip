package com.example.vocaflip.flashcard.repository;

import com.example.vocaflip.flashcard.entity.Flashcard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByFlashcardSetIdOrderByOrderIndexAsc(Long flashcardSetId);

    List<Flashcard> findByFlashcardSetIdAndFlashcardSetUserEmailOrderByOrderIndexAsc(Long flashcardSetId, String email);

    long countByFlashcardSetId(Long flashcardSetId);

    Optional<Flashcard> findByIdAndFlashcardSetIdAndFlashcardSetUserEmail(Long id, Long flashcardSetId, String email);

    void deleteByFlashcardSetId(Long flashcardSetId);
}
