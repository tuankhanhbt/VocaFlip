package com.example.vocaflip.flashcard.entity;

import com.example.vocaflip.common.entity.BaseEntity;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "flashcards")
public class Flashcard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashcardSet flashcardSet;

    @Column(nullable = false, length = 500)
    private String frontText;

    @Column(nullable = false, length = 500)
    private String backText;

    @Column(columnDefinition = "TEXT")
    private String exampleText;

    @Column(columnDefinition = "TEXT")
    private String noteText;

    @Column(nullable = false)
    private Integer orderIndex = 0;
}
