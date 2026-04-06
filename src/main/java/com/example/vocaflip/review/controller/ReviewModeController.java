package com.example.vocaflip.review.controller;

import com.example.vocaflip.review.dto.ReviewModeResponse;
import com.example.vocaflip.review.dto.UpdateReviewProgressRequest;
import com.example.vocaflip.review.service.ReviewModeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review-mode")
@RequiredArgsConstructor
public class ReviewModeController {

    private final ReviewModeService reviewModeService;

    @GetMapping("/flashcard-sets/{setId}")
    public ResponseEntity<ReviewModeResponse> getReviewMode(
            @PathVariable Long setId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(reviewModeService.getReviewMode(setId, authentication));
    }

    @PutMapping("/flashcard-sets/{setId}/progress")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long setId,
            @Valid @RequestBody UpdateReviewProgressRequest request,
            Authentication authentication
    ) {
        reviewModeService.updateReviewProgress(setId, request.getCurrentCardIndex(), authentication);
        return ResponseEntity.ok().build();
    }
}