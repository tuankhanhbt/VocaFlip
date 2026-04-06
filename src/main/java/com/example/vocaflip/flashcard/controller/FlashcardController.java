package com.example.vocaflip.flashcard.controller;

import com.example.vocaflip.flashcard.dto.FlashcardRequest;
import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import com.example.vocaflip.flashcard.service.FlashcardService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flashcard-sets/{setId}/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @GetMapping
    public ResponseEntity<List<FlashcardResponse>> getAll(@PathVariable Long setId, Authentication authentication) {
        return ResponseEntity.ok(flashcardService.getAllBySetId(setId, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashcardResponse> getById(
        @PathVariable Long setId,
        @PathVariable Long id,
        Authentication authentication
    ) {
        return ResponseEntity.ok(flashcardService.getById(setId, id, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<FlashcardResponse> create(
        @PathVariable Long setId,
        @Valid @RequestBody FlashcardRequest request,
        Authentication authentication
    ) {
        FlashcardResponse response = flashcardService.create(setId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashcardResponse> update(
        @PathVariable Long setId,
        @PathVariable Long id,
        @Valid @RequestBody FlashcardRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(flashcardService.update(setId, id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable Long setId,
        @PathVariable Long id,
        Authentication authentication
    ) {
        flashcardService.delete(setId, id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
