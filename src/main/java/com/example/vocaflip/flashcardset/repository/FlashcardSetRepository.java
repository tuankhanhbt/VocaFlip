package com.example.vocaflip.flashcardset.repository;

import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {

    List<FlashcardSet> findByUserId(Long userId);

    List<FlashcardSet> findByUserEmailOrderByCreatedAtDesc(String email);

    Optional<FlashcardSet> findByIdAndUserEmail(Long id, String email);

    boolean existsByIdAndUserEmail(Long id, String email);
    
    Optional<FlashcardSet> findByShareCode(String shareCode);
    
    boolean existsByShareCode(String shareCode);
}
