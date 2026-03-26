package com.example.vocaflip.flashcardset.controller;

import com.example.vocaflip.flashcardset.dto.FlashcardSetRequest;
import com.example.vocaflip.flashcardset.dto.FlashcardSetResponse;
import com.example.vocaflip.flashcardset.service.FlashcardSetService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    // TODO: Replace hardcoded userId with authenticated user
    private static final Long TEMP_USER_ID = 1L;

    @GetMapping
    public ResponseEntity<List<FlashcardSetResponse>> getAll() {
        return ResponseEntity.ok(flashcardSetService.getAllByUserId(TEMP_USER_ID));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(flashcardSetService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FlashcardSetResponse> create(@Valid @RequestBody FlashcardSetRequest request) {
        FlashcardSetResponse response = flashcardSetService.create(TEMP_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FlashcardSetRequest request) {
        return ResponseEntity.ok(flashcardSetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        flashcardSetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
