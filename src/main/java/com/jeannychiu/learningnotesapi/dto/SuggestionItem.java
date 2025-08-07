package com.jeannychiu.learningnotesapi.dto;

import lombok.Data;

@Data
public class SuggestionItem {
    private Long id;
    private String title;
    private String type;
    private String matchedText;
}
