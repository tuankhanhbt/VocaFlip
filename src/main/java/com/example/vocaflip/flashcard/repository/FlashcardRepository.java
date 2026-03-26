package com.example.vocaflip.flashcard.repository;

import com.example.vocaflip.flashcard.entity.Flashcard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByFlashcardSetIdOrderByOrderIndexAsc(Long flashcardSetId);

    long countByFlashcardSetId(Long flashcardSetId);
}
