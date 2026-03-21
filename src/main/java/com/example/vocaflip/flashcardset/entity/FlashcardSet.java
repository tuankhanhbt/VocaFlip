package com.example.vocaflip.flashcardset.entity;

import com.example.vocaflip.common.entity.BaseEntity;
import com.example.vocaflip.user.entity.User;
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
@Table(name = "flashcard_sets")
public class FlashcardSet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String sourceLanguage = "en";

    @Column(nullable = false, length = 30)
    private String targetLanguage = "vi";

    @Column(nullable = false)
    private Boolean archived = false;
}
