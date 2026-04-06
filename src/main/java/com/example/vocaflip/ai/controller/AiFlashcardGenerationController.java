package com.example.vocaflip.ai.controller;

import com.example.vocaflip.ai.dto.GenerateFlashcardSetRequest;
import com.example.vocaflip.ai.dto.GeneratedFlashcardSetResponse;
import com.example.vocaflip.ai.service.AiFlashcardGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/flashcard-sets")
@RequiredArgsConstructor
public class AiFlashcardGenerationController {

    private final AiFlashcardGenerationService aiFlashcardGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<GeneratedFlashcardSetResponse> generateAndSave(
        @Valid @RequestBody GenerateFlashcardSetRequest request,
        Authentication authentication
    ) {
        GeneratedFlashcardSetResponse response =
            aiFlashcardGenerationService.generateAndSave(authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}