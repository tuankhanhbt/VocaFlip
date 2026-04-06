package com.example.vocaflip.ai.service;

import com.example.vocaflip.ai.dto.GenerateFlashcardSetRequest;
import com.example.vocaflip.ai.dto.GeneratedFlashcardSetResponse;

public interface AiFlashcardGenerationService {
    GeneratedFlashcardSetResponse generateAndSave(String email, GenerateFlashcardSetRequest request);
}