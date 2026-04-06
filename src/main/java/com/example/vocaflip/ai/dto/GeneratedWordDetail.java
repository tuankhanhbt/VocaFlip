package com.example.vocaflip.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneratedWordDetail {
    private String word;
    private String phonetic;
    private String audioUrl;
}