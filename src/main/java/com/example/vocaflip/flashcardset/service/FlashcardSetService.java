package com.example.vocaflip.flashcardset.service;

import com.example.vocaflip.common.exception.ResourceNotFoundException;
import com.example.vocaflip.flashcard.repository.FlashcardRepository;
import com.example.vocaflip.flashcardset.dto.FlashcardSetRequest;
import com.example.vocaflip.flashcardset.dto.FlashcardSetResponse;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.flashcardset.repository.FlashcardSetRepository;
import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;

    public List<FlashcardSetResponse> getAllByUserId(Long userId) {
        return flashcardSetRepository.findByUserId(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    public FlashcardSetResponse getById(Long id) {
        FlashcardSet set = flashcardSetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("FlashcardSet", id));
        return toResponse(set);
    }

    @Transactional
    public FlashcardSetResponse create(Long userId, FlashcardSetRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        FlashcardSet set = new FlashcardSet();
        set.setUser(user);
        set.setTitle(request.getTitle());
        set.setDescription(request.getDescription());

        if (request.getSourceLanguage() != null) {
            set.setSourceLanguage(request.getSourceLanguage());
        }
        if (request.getTargetLanguage() != null) {
            set.setTargetLanguage(request.getTargetLanguage());
        }

        FlashcardSet saved = flashcardSetRepository.save(set);
        return toResponse(saved);
    }

    @Transactional
    public FlashcardSetResponse update(Long id, FlashcardSetRequest request) {
        FlashcardSet set = flashcardSetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("FlashcardSet", id));

        set.setTitle(request.getTitle());
        set.setDescription(request.getDescription());

        if (request.getSourceLanguage() != null) {
            set.setSourceLanguage(request.getSourceLanguage());
        }
        if (request.getTargetLanguage() != null) {
            set.setTargetLanguage(request.getTargetLanguage());
        }

        FlashcardSet saved = flashcardSetRepository.save(set);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!flashcardSetRepository.existsById(id)) {
            throw new ResourceNotFoundException("FlashcardSet", id);
        }
        flashcardSetRepository.deleteById(id);
    }

    private FlashcardSetResponse toResponse(FlashcardSet set) {
        return FlashcardSetResponse.builder()
            .id(set.getId())
            .title(set.getTitle())
            .description(set.getDescription())
            .sourceLanguage(set.getSourceLanguage())
            .targetLanguage(set.getTargetLanguage())
            .archived(set.getArchived())
            .cardCount(flashcardRepository.countByFlashcardSetId(set.getId()))
            .createdAt(set.getCreatedAt())
            .updatedAt(set.getUpdatedAt())
            .build();
    }
}
