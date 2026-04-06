package com.example.vocaflip.ai.service.impl;

import com.example.vocaflip.ai.dto.GenerateFlashcardSetRequest;
import com.example.vocaflip.ai.dto.GeneratedCardContent;
import com.example.vocaflip.ai.dto.GeneratedFlashcardSetResponse;
import com.example.vocaflip.ai.dto.GeneratedWordDetail;
import com.example.vocaflip.ai.service.AiFlashcardGenerationService;
import com.example.vocaflip.ai.service.DictionaryService;
import com.example.vocaflip.ai.service.GeminiService;
import com.example.vocaflip.common.exception.ResourceNotFoundException;
import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import com.example.vocaflip.flashcard.entity.Flashcard;
import com.example.vocaflip.flashcard.entity.FrontContentType;
import com.example.vocaflip.flashcard.repository.FlashcardRepository;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.flashcardset.repository.FlashcardSetRepository;
import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiFlashcardGenerationServiceImpl implements AiFlashcardGenerationService {

    private final GeminiService geminiService;
    private final DictionaryService dictionaryService;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GeneratedFlashcardSetResponse generateAndSave(String email, GenerateFlashcardSetRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(email))
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

        FlashcardSet set = new FlashcardSet();
        set.setUser(user);
        set.setTitle(request.getTitle().trim());
        set.setDescription(request.getDescription());
        set.setSourceLanguage(defaultIfBlank(request.getSourceLanguage(), "en"));
        set.setTargetLanguage(defaultIfBlank(request.getTargetLanguage(), "vi"));

        FlashcardSet savedSet = flashcardSetRepository.save(set);

        List<GeneratedCardContent> generatedCards = geminiService.generateCardsByTopic(
            request.getTopic(),
            request.getCount(),
            request.getTargetLanguage()
        );

        List<FlashcardResponse> savedCards = new ArrayList<>();

        for (int i = 0; i < generatedCards.size(); i++) {
            GeneratedCardContent generated = generatedCards.get(i);
            GeneratedWordDetail detail = dictionaryService.getWordDetail(generated.getWord());

            Flashcard card = new Flashcard();
            card.setFlashcardSet(savedSet);
            card.setFrontContentType(FrontContentType.TEXT);
            card.setFrontText(generated.getWord());
            card.setBackText(generated.getMeaning());
            card.setExampleText(generated.getExample());
            card.setPhonetic(detail.getPhonetic());
            card.setAudioUrl(detail.getAudioUrl());
            card.setOrderIndex(i);

            Flashcard savedCard = flashcardRepository.save(card);
            savedCards.add(toResponse(savedCard));
        }

        return GeneratedFlashcardSetResponse.builder()
            .flashcardSetId(savedSet.getId())
            .title(savedSet.getTitle())
            .description(savedSet.getDescription())
            .topic(request.getTopic())
            .sourceLanguage(savedSet.getSourceLanguage())
            .targetLanguage(savedSet.getTargetLanguage())
            .requestedCount(request.getCount())
            .actualCount(savedCards.size())
            .cards(savedCards)
            .createdAt(savedSet.getCreatedAt())
            .build();
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
            .phonetic(card.getPhonetic())
            .audioUrl(card.getAudioUrl())
            .orderIndex(card.getOrderIndex())
            .createdAt(card.getCreatedAt())
            .updatedAt(card.getUpdatedAt())
            .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.trim().isBlank() ? defaultValue : value.trim();
    }
}