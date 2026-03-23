package com.example.vocaflip.flashcard.service;

import com.example.vocaflip.common.exception.ResourceNotFoundException;
import com.example.vocaflip.flashcard.dto.FlashcardRequest;
import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import com.example.vocaflip.flashcard.entity.Flashcard;
import com.example.vocaflip.flashcard.entity.FrontContentType;
import com.example.vocaflip.flashcard.repository.FlashcardRepository;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.flashcardset.repository.FlashcardSetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;

    public List<FlashcardResponse> getAllBySetId(Long setId) {
        if (!flashcardSetRepository.existsById(setId)) {
            throw new ResourceNotFoundException("FlashcardSet", setId);
        }
        return flashcardRepository.findByFlashcardSetIdOrderByOrderIndexAsc(setId).stream()
            .map(this::toResponse)
            .toList();
    }

    public FlashcardResponse getById(Long setId, Long cardId) {
        Flashcard card = findCardInSet(setId, cardId);
        return toResponse(card);
    }

    @Transactional
    public FlashcardResponse create(Long setId, FlashcardRequest request) {
        FlashcardSet set = flashcardSetRepository.findById(setId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashcardSet", setId));

        Flashcard card = new Flashcard();
        card.setFlashcardSet(set);
        applyRequest(card, request);

        if (card.getOrderIndex() == null || card.getOrderIndex() == 0) {
            long count = flashcardRepository.countByFlashcardSetId(setId);
            card.setOrderIndex((int) count);
        }

        Flashcard saved = flashcardRepository.save(card);
        return toResponse(saved);
    }

    @Transactional
    public FlashcardResponse update(Long setId, Long cardId, FlashcardRequest request) {
        Flashcard card = findCardInSet(setId, cardId);
        applyRequest(card, request);

        Flashcard saved = flashcardRepository.save(card);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long setId, Long cardId) {
        Flashcard card = findCardInSet(setId, cardId);
        flashcardRepository.delete(card);
    }

    private Flashcard findCardInSet(Long setId, Long cardId) {
        if (!flashcardSetRepository.existsById(setId)) {
            throw new ResourceNotFoundException("FlashcardSet", setId);
        }
        Flashcard card = flashcardRepository.findById(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("Flashcard", cardId));
        if (!card.getFlashcardSet().getId().equals(setId)) {
            throw new ResourceNotFoundException("Flashcard", cardId);
        }
        return card;
    }

    private void applyRequest(Flashcard card, FlashcardRequest request) {
        card.setFrontText(request.getFrontText());
        card.setBackText(request.getBackText());
        card.setFrontImageUrl(request.getFrontImageUrl());
        card.setExampleText(request.getExampleText());
        card.setNoteText(request.getNoteText());

        if (request.getFrontContentType() != null) {
            card.setFrontContentType(FrontContentType.valueOf(request.getFrontContentType().toUpperCase()));
        }
        if (request.getOrderIndex() != null) {
            card.setOrderIndex(request.getOrderIndex());
        }
    }

    private FlashcardResponse toResponse(Flashcard card) {
        return FlashcardResponse.builder()
            .id(card.getId())
            .flashcardSetId(card.getFlashcardSet().getId())
            .frontContentType(card.getFrontContentType().name())
            .frontText(card.getFrontText())
            .frontImageUrl(card.getFrontImageUrl())
            .backText(card.getBackText())
            .exampleText(card.getExampleText())
            .noteText(card.getNoteText())
            .orderIndex(card.getOrderIndex())
            .createdAt(card.getCreatedAt())
            .updatedAt(card.getUpdatedAt())
            .build();
    }
}
