package com.example.vocaflip.flashcardset.repository;

import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {

    List<FlashcardSet> findByUserId(Long userId);
}
