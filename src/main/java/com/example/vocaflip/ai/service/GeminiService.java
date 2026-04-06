package com.example.vocaflip.ai.service;

import com.example.vocaflip.ai.dto.GeneratedCardContent;
import java.util.List;

public interface GeminiService {
    List<GeneratedCardContent> generateCardsByTopic(String topic, int count, String targetLanguage);
}