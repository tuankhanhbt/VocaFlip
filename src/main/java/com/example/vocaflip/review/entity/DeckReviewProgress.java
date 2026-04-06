package com.example.vocaflip.review.entity;

import com.example.vocaflip.common.entity.BaseEntity;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "deck_review_progress",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_deck_review_progress_user_set",
            columnNames = {"user_id", "flashcard_set_id"}
        )
    }
)
public class DeckReviewProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashcardSet flashcardSet;

    @Column(nullable = false)
    private Integer currentCardIndex = 0;

    private LocalDateTime lastReviewedAt;
}