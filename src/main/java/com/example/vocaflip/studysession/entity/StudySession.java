package com.example.vocaflip.studysession.entity;

import com.example.vocaflip.common.entity.BaseEntity;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "study_sessions")
public class StudySession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashcardSet flashcardSet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudySessionStatus status = StudySessionStatus.IN_PROGRESS;

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private Integer totalCards = 0;

    @Column(nullable = false)
    private Integer reviewedCards = 0;

    @Column(nullable = false)
    private Integer againCount = 0;

    @Column(nullable = false)
    private Integer hardCount = 0;

    @Column(nullable = false)
    private Integer goodCount = 0;

    @Column(nullable = false)
    private Integer easyCount = 0;
}
