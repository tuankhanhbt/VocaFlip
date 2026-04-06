package com.example.vocaflip.ai.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeminiWordListResponse {
    private List<String> words;
}