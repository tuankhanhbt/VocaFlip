package com.example.vocaflip.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneratedCardContent {

    private String word;
    private String meaning;
    private String example;
}