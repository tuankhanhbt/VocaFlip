package com.example.vocaflip.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeminiGeneratedCardItemResponse {

    private String word;
    private String meaning;
    private String example;
}