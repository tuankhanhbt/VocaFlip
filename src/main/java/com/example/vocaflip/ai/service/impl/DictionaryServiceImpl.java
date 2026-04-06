package com.example.vocaflip.ai.service.impl;

import com.example.vocaflip.ai.dto.DictionaryEntryDto;
import com.example.vocaflip.ai.dto.GeneratedWordDetail;
import com.example.vocaflip.ai.dto.PhoneticDto;
import com.example.vocaflip.ai.service.DictionaryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DictionaryServiceImpl implements DictionaryService {

    private final RestClient restClient;
    private final String dictionaryBaseUrl;

    public DictionaryServiceImpl(
        RestClient restClient,
        @Value("${dictionary.api.base-url}") String dictionaryBaseUrl
    ) {
        this.restClient = restClient;
        this.dictionaryBaseUrl = dictionaryBaseUrl;
    }

    @Override
    public GeneratedWordDetail getWordDetail(String word) {
        try {
            DictionaryEntryDto[] response = restClient.get()
                .uri(dictionaryBaseUrl + "/" + word)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Dictionary API 4xx for word: " + word);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Dictionary API 5xx for word: " + word);
                })
                .body(DictionaryEntryDto[].class);

            if (response == null || response.length == 0) {
                return new GeneratedWordDetail(word, null, null);
            }

            DictionaryEntryDto entry = response[0];
            String phonetic = extractPhonetic(entry);
            String audioUrl = extractAudio(entry);

            return new GeneratedWordDetail(word, phonetic, audioUrl);

        } catch (Exception e) {
            return new GeneratedWordDetail(word, null, null);
        }
    }

    private String extractPhonetic(DictionaryEntryDto entry) {
        if (entry.getPhonetic() != null && !entry.getPhonetic().isBlank()) {
            return entry.getPhonetic();
        }

        List<PhoneticDto> phonetics = entry.getPhonetics();
        if (phonetics != null) {
            for (PhoneticDto phoneticDto : phonetics) {
                if (phoneticDto.getText() != null && !phoneticDto.getText().isBlank()) {
                    return phoneticDto.getText();
                }
            }
        }

        return null;
    }

    private String extractAudio(DictionaryEntryDto entry) {
        List<PhoneticDto> phonetics = entry.getPhonetics();
        if (phonetics != null) {
            for (PhoneticDto phoneticDto : phonetics) {
                if (phoneticDto.getAudio() != null && !phoneticDto.getAudio().isBlank()) {
                    return phoneticDto.getAudio();
                }
            }
        }

        return null;
    }
}