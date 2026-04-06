package com.example.vocaflip.flashcard.service;

import com.example.vocaflip.common.exception.ResourceNotFoundException;
import com.example.vocaflip.flashcard.dto.FlashcardRequest;
import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import com.example.vocaflip.flashcard.entity.Flashcard;
import com.example.vocaflip.flashcard.entity.FrontContentType;
import com.example.vocaflip.flashcard.repository.FlashcardRepository;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.flashcardset.entity.SetVisibility;
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

    public List<FlashcardResponse> getAllBySetId(Long setId, String email) {
        FlashcardSet set = findOwnedSet(setId, email);
        return flashcardRepository.findByFlashcardSetIdAndFlashcardSetUserEmailOrderByOrderIndexAsc(
                set.getId(),
                normalizeEmail(email)
            ).stream()
            .map(this::toResponse)
            .toList();
    }
    
    public List<FlashcardResponse> getAllByShareCode(String shareCode) {
        FlashcardSet set = flashcardSetRepository.findByShareCode(shareCode)
            .orElseThrow(() -> new ResourceNotFoundException("SharedFlashcardSet", shareCode));
            
        if (set.getVisibility() != SetVisibility.PUBLIC) {
            throw new ResourceNotFoundException("SharedFlashcardSet", shareCode);
        }
        
        return flashcardRepository.findByFlashcardSetIdOrderByOrderIndexAsc(set.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    public FlashcardResponse getById(Long setId, Long cardId, String email) {
        Flashcard card = findCardInSet(setId, cardId, email);
        return toResponse(card);
    }

    @Transactional
    public FlashcardResponse create(Long setId, FlashcardRequest request, String email) {
        FlashcardSet set = findOwnedSet(setId, email);

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
    public FlashcardResponse update(Long setId, Long cardId, FlashcardRequest request, String email) {
        Flashcard card = findCardInSet(setId, cardId, email);
        applyRequest(card, request);

        Flashcard saved = flashcardRepository.save(card);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long setId, Long cardId, String email) {
        Flashcard card = findCardInSet(setId, cardId, email);
        flashcardRepository.delete(card);
    }

    private Flashcard findCardInSet(Long setId, Long cardId, String email) {
        findOwnedSet(setId, email);

        return flashcardRepository.findByIdAndFlashcardSetIdAndFlashcardSetUserEmail(
                cardId,
                setId,
                normalizeEmail(email)
            )
            .orElseThrow(() -> new ResourceNotFoundException("Flashcard", cardId));
    }

    private FlashcardSet findOwnedSet(Long setId, String email) {
        return flashcardSetRepository.findByIdAndUserEmail(setId, normalizeEmail(email))
            .orElseThrow(() -> new ResourceNotFoundException("FlashcardSet", setId));
    }

    private void applyRequest(Flashcard card, FlashcardRequest request) {
        FrontContentType frontContentType = request.getFrontContentType() == null
            ? FrontContentType.TEXT
            : FrontContentType.valueOf(request.getFrontContentType().trim().toUpperCase());

        card.setFrontContentType(frontContentType);
        card.setFrontText(request.getFrontText() == null ? null : request.getFrontText().trim());
        card.setBackText(request.getBackText());
        card.setFrontImageUrl(request.getFrontImageUrl() == null ? null : request.getFrontImageUrl().trim());
        card.setExampleText(request.getExampleText());
        card.setNoteText(request.getNoteText());
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

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
