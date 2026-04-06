package com.example.vocaflip.ai.service.impl;

import com.example.vocaflip.ai.dto.GeneratedCardContent;
import com.example.vocaflip.ai.dto.GeminiGeneratedCardItemResponse;
import com.example.vocaflip.ai.dto.GeminiGeneratedCardListResponse;
import com.example.vocaflip.ai.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class GeminiServiceImpl implements GeminiService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Client client;
    private final String model;

    public GeminiServiceImpl(
        @Value("${google.ai.api-key:}") String apiKey,
        @Value("${google.ai.model}") String model
    ) {
        this.model = model;
        this.client = StringUtils.hasText(apiKey)
            ? Client.builder().apiKey(apiKey).build()
            : null;
    }

    @Override
    public List<GeneratedCardContent> generateCardsByTopic(String topic, int count, String targetLanguage) {
        if (client == null) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Gemini API is not configured. Please set GEMINI_API_KEY."
            );
        }

        try {
            String prompt = """
                Generate exactly %d English vocabulary flashcards for the topic: "%s".

                Requirements:
                - Each item must contain:
                  1. "word": one simple English word
                  2. "meaning": the meaning of that word in %s
                  3. "example": one short and simple English example sentence using that word
                - Prefer common vocabulary useful for learners.
                - No duplicate words.
                - No explanations outside JSON.
                - No markdown.
                - Return JSON only in this exact format:
                {
                  "cards": [
                    {
                      "word": "apple",
                      "meaning": "quả táo",
                      "example": "I eat an apple every day."
                    }
                  ]
                }
                """.formatted(count, topic, normalizeLanguage(targetLanguage));

            GenerateContentResponse response = client.models.generateContent(model, prompt, null);

            String rawText = response.text();
            if (rawText == null || rawText.isBlank()) {
                throw new RuntimeException("Gemini returned empty response");
            }

            String json = extractJson(rawText);

            GeminiGeneratedCardListResponse parsed =
                objectMapper.readValue(json, GeminiGeneratedCardListResponse.class);

            if (parsed.getCards() == null || parsed.getCards().isEmpty()) {
                throw new RuntimeException("Gemini returned no cards");
            }

            List<GeneratedCardContent> results = new ArrayList<>();
            Set<String> uniqueWords = new LinkedHashSet<>();

            for (GeminiGeneratedCardItemResponse item : parsed.getCards()) {
                if (item == null) {
                    continue;
                }

                String word = safeTrimLower(item.getWord());
                String meaning = safeTrim(item.getMeaning());
                String example = safeTrim(item.getExample());

                if (word == null || word.contains(" ")) {
                    continue;
                }
                if (meaning == null || meaning.isBlank()) {
                    continue;
                }
                if (example == null || example.isBlank()) {
                    continue;
                }
                if (uniqueWords.contains(word)) {
                    continue;
                }

                uniqueWords.add(word);
                results.add(new GeneratedCardContent(word, meaning, example));

                if (results.size() >= count) {
                    break;
                }
            }

            if (results.isEmpty()) {
                throw new RuntimeException("Gemini returned invalid card data");
            }

            return results;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate cards from Gemini: " + e.getMessage(), e);
        }
    }

    private String extractJson(String rawText) {
        String text = rawText.trim();

        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
        }

        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }

        return text;
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private String safeTrimLower(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private String normalizeLanguage(String targetLanguage) {
        if (targetLanguage == null || targetLanguage.isBlank()) {
            return "Vietnamese";
        }

        String normalized = targetLanguage.trim().toLowerCase();
        return switch (normalized) {
            case "vi", "vietnamese" -> "Vietnamese";
            case "en", "english" -> "English";
            default -> targetLanguage.trim();
        };
    }
}
