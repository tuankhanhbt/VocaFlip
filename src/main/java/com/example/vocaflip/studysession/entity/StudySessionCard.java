package com.example.vocaflip.studysession.entity;

import com.example.vocaflip.common.entity.BaseEntity;
import com.example.vocaflip.flashcard.entity.Flashcard;
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
@Table(name = "study_session_cards")
public class StudySessionCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_session_id", nullable = false)
    private StudySession studySession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(nullable = false)
    private Integer orderIndex = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewRating rating;

    @Column(nullable = false)
    private LocalDateTime reviewedAt = LocalDateTime.now();

     @Column(length = 500)
    private String selectedAnswer;

    @Column(length = 500)
    private String correctAnswer;

    @Column(name = "selected_flashcard_id")
    private Long selectedFlashcardId;

    @Column(name = "correct_flashcard_id")
    private Long correctFlashcardId;
    
    @Column(nullable = false)
    private Boolean correct = false;

}
