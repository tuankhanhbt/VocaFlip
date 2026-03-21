package com.example.vocaflip.progress.entity;

import com.example.vocaflip.common.entity.BaseEntity;
import com.example.vocaflip.flashcard.entity.Flashcard;
import com.example.vocaflip.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "user_card_progress",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_card_progress_user_flashcard", columnNames = {"user_id", "flashcard_id"})
    }
)
public class UserCardProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardProgressStatus status = CardProgressStatus.NEW;

    private LocalDateTime lastReviewedAt;

    private LocalDateTime nextReviewAt;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Integer correctStreak = 0;

    @Column(nullable = false)
    private Integer intervalDays = 0;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal easinessFactor = BigDecimal.valueOf(2.50);

    @Column(nullable = false)
    private Integer lapseCount = 0;
}
