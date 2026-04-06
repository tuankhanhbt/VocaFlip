package com.example.vocaflip.flashcardset.controller;

import com.example.vocaflip.flashcardset.dto.SharedFlashcardSetResponse;
import com.example.vocaflip.flashcardset.service.FlashcardSetService;
import com.example.vocaflip.flashcard.dto.FlashcardResponse;
import com.example.vocaflip.flashcard.service.FlashcardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shared/flashcard-sets")
@RequiredArgsConstructor
public class SharedFlashcardSetController {

    private final FlashcardSetService flashcardSetService;
    private final FlashcardService flashcardService;

    @GetMapping("/{shareCode}")
    public ResponseEntity<SharedFlashcardSetResponse> getByShareCode(@PathVariable String shareCode) {
        return ResponseEntity.ok(flashcardSetService.getByShareCode(shareCode));
    }

    @GetMapping("/{shareCode}/flashcards")
    public ResponseEntity<List<FlashcardResponse>> getFlashcardsByShareCode(@PathVariable String shareCode) {
        return ResponseEntity.ok(flashcardService.getAllByShareCode(shareCode));
    }
}
