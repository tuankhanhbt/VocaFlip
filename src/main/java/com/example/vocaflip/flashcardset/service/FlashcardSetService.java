package com.example.vocaflip.flashcardset.service;

import com.example.vocaflip.common.exception.ResourceNotFoundException;
import com.example.vocaflip.flashcard.repository.FlashcardRepository;
import com.example.vocaflip.flashcardset.dto.FlashcardSetRequest;
import com.example.vocaflip.flashcardset.dto.FlashcardSetResponse;
import com.example.vocaflip.flashcardset.dto.ShareSettingsRequest;
import com.example.vocaflip.flashcardset.dto.ShareLinkResponse;
import com.example.vocaflip.flashcardset.dto.SharedFlashcardSetResponse;
import com.example.vocaflip.flashcardset.entity.FlashcardSet;
import com.example.vocaflip.flashcardset.entity.SetVisibility;
import com.example.vocaflip.flashcardset.repository.FlashcardSetRepository;
import com.example.vocaflip.user.entity.User;
import com.example.vocaflip.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public List<FlashcardSetResponse> getAllByUserEmail(String email) {
        return flashcardSetRepository.findByUserEmailOrderByCreatedAtDesc(normalizeEmail(email)).stream()
            .map(this::toResponse)
            .toList();
    }

    public FlashcardSetResponse getById(Long id, String email) {
        FlashcardSet set = findOwnedSet(id, email);
        return toResponse(set);
    }

    @Transactional
    public FlashcardSetResponse create(String email, FlashcardSetRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(email))
            .orElseThrow(() -> new ResourceNotFoundException("User", email));

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
    public FlashcardSetResponse update(Long id, String email, FlashcardSetRequest request) {
        FlashcardSet set = findOwnedSet(id, email);

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
    public void delete(Long id, String email) {
        FlashcardSet set = findOwnedSet(id, email);
        flashcardSetRepository.delete(set);
    }
    
    @Transactional
    public FlashcardSetResponse updateShareSettings(Long id, String email, ShareSettingsRequest request) {
        FlashcardSet set = findOwnedSet(id, email);
        
        if (request.visibility() != null) {
            set.setVisibility(parseVisibility(request.visibility()));
        }
        if (request.allowCopy() != null) {
            set.setAllowCopy(request.allowCopy());
        }
        if (request.allowReview() != null) {
            set.setAllowReview(request.allowReview());
        }
        
        return toResponse(flashcardSetRepository.save(set));
    }
    
    @Transactional
    public ShareLinkResponse generateShareLink(Long id, String email) {
        FlashcardSet set = findOwnedSet(id, email);
        
        String newCode;
        do {
            newCode = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        } while (flashcardSetRepository.existsByShareCode(newCode));
        
        set.setShareCode(newCode);
        flashcardSetRepository.save(set);
        
        return new ShareLinkResponse(newCode, frontendUrl + "/shared/" + newCode);
    }
    
    public SharedFlashcardSetResponse getByShareCode(String shareCode) {
        FlashcardSet set = flashcardSetRepository.findByShareCode(shareCode)
            .orElseThrow(() -> new ResourceNotFoundException("SharedFlashcardSet", shareCode));
            
        if (set.getVisibility() != SetVisibility.PUBLIC) {
            throw new ResourceNotFoundException("SharedFlashcardSet", shareCode);
        }
        
        return new SharedFlashcardSetResponse(
            set.getId(),
            set.getTitle(),
            set.getDescription(),
            set.getSourceLanguage(),
            set.getTargetLanguage(),
            flashcardRepository.countByFlashcardSetId(set.getId()),
            set.getAllowCopy(),
            set.getAllowReview()
        );
    }

    private FlashcardSetResponse toResponse(FlashcardSet set) {
        return new FlashcardSetResponse(
            set.getId(),
            set.getTitle(),
            set.getDescription(),
            set.getSourceLanguage(),
            set.getTargetLanguage(),
            set.getArchived(),
            flashcardRepository.countByFlashcardSetId(set.getId()),
            set.getCreatedAt(),
            set.getUpdatedAt(),
            set.getVisibility().name(),
            set.getShareCode(),
            set.getAllowCopy(),
            set.getAllowReview()
        );
    }

    private FlashcardSet findOwnedSet(Long id, String email) {
        return flashcardSetRepository.findByIdAndUserEmail(id, normalizeEmail(email))
            .orElseThrow(() -> new ResourceNotFoundException("FlashcardSet", id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private SetVisibility parseVisibility(String rawVisibility) {
        String normalizedVisibility = rawVisibility.trim().toUpperCase();

        try {
            SetVisibility visibility = SetVisibility.valueOf(normalizedVisibility);

            if (visibility == SetVisibility.SHARED) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "SHARED visibility is not supported yet"
                );
            }

            return visibility;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Visibility must be one of: PRIVATE, PUBLIC"
            );
        }
    }
}
