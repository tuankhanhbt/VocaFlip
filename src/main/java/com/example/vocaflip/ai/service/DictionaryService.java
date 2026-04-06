package com.example.vocaflip.ai.service;

import com.example.vocaflip.ai.dto.GeneratedWordDetail;

public interface DictionaryService {
    GeneratedWordDetail getWordDetail(String word);
}