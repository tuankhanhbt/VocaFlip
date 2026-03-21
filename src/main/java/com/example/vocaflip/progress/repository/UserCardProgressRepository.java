package com.example.vocaflip.progress.repository;

import com.example.vocaflip.progress.entity.UserCardProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCardProgressRepository extends JpaRepository<UserCardProgress, Long> {

    Optional<UserCardProgress> findByUserIdAndFlashcardId(Long userId, Long flashcardId);

    List<UserCardProgress> findByUserId(Long userId);
}
