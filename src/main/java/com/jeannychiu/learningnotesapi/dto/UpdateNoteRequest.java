package com.jeannychiu.learningnotesapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateNoteRequest {
    @NotBlank(message = "標題不能為空")
    private String title;

    @Size(max = 500, message = "內容不能超過 500 字")
    private String content;

    @NotNull
    private Integer version;

    private Set<String> tagNames;
}
