package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetNotes() throws Exception {
        // 測試 GET /notes
        // 1. 準備測試資料
        Note note = new Note();
        note.setId(1L);
        note.setTitle("測試標題");
        note.setContent("測試內容");
        note.setUserEmail("test@example.com");

        List<Note> notes = Arrays.asList(note);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Note> page = new PageImpl<>(notes, pageable, notes.size());

        // 2. 設定Mock行為
        when(noteService.getAllNotes(any(), anyString(), anyBoolean())).thenReturn(page);

        // 3. 建立 RequestBuilder
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/notes")
                .param("page", "0")
                .param("size", "10");

        // 4. 執行測試
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", equalTo("測試標題")))
                .andExpect(jsonPath("$.content[0].content", equalTo("測試內容")))
                .andExpect(jsonPath("$.content[0].userEmail", equalTo("test@example.com")))
                .andExpect(jsonPath("$.totalElements", equalTo(1)))
                .andExpect(jsonPath("$.pageable.pageNumber", equalTo(0)))
                .andExpect(jsonPath("$.pageable.pageSize", equalTo(10)))
                .andReturn();

        // 5. 檢查回應內容
        String responseContent = mvcResult.getResponse().getContentAsString();
        System.out.println("Response回應 : " + responseContent);
    }
}