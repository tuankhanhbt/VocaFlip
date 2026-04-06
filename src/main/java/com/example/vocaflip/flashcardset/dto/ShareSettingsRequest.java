package com.example.vocaflip.flashcardset.dto;

public record ShareSettingsRequest(
    String visibility,
    Boolean allowCopy,
    Boolean allowReview
) {}
