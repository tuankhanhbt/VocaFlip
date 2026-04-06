package com.example.vocaflip.flashcardset.controller;

import com.example.vocaflip.flashcardset.dto.FlashcardSetRequest;
import com.example.vocaflip.flashcardset.dto.FlashcardSetResponse;
import com.example.vocaflip.flashcardset.dto.ShareLinkResponse;
import com.example.vocaflip.flashcardset.dto.ShareSettingsRequest;
import com.example.vocaflip.flashcardset.service.FlashcardSetService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flashcard-sets")
@RequiredArgsConstructor
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;

    @GetMapping
    public ResponseEntity<List<FlashcardSetResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(flashcardSetService.getAllByUserEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(flashcardSetService.getById(id, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<FlashcardSetResponse> create(
        @Valid @RequestBody FlashcardSetRequest request,
        Authentication authentication
    ) {
        FlashcardSetResponse response = flashcardSetService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody FlashcardSetRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(flashcardSetService.update(id, authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        flashcardSetService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/share-settings")
    public ResponseEntity<FlashcardSetResponse> updateShareSettings(
        @PathVariable Long id,
        @RequestBody ShareSettingsRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(flashcardSetService.updateShareSettings(id, authentication.getName(), request));
    }
    
    @PostMapping("/{id}/share-link")
    public ResponseEntity<ShareLinkResponse> generateShareLink(
        @PathVariable Long id,
        Authentication authentication
    ) {
        return ResponseEntity.ok(flashcardSetService.generateShareLink(id, authentication.getName()));
    }
}
