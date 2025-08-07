package com.jeannychiu.learningnotesapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchSuggestionsResponse {
    private List<SuggestionItem> suggestions;
}
