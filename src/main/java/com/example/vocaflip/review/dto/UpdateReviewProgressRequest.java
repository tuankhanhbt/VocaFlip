package com.example.vocaflip.review.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReviewProgressRequest {

    @NotNull
    @Min(0)
    private Integer currentCardIndex;
}