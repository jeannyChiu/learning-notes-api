package com.jeannychiu.learningnotesapi.service;

import com.jeannychiu.learningnotesapi.model.Note;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NoteServiceTest {
    @Autowired
    private NoteService noteService;

    private String testUserEmail;

    @BeforeEach
    void setUp() {
        testUserEmail = "test@example.com";
    }

    @Test
    void testCreateNote() {
        // 測試創建筆記
        // 準備測試資料
        Note note = new Note();
        note.setTitle("測試標題 with English & 特殊符號!@#");
        note.setContent("這是一段較長的測試內容...");

        // 執行測試
        Note createdNote = noteService.createNote(note, testUserEmail);

        // 驗證結果
        // 基本驗證
        assertNotNull(createdNote);
        assertNotNull(createdNote.getId());

        // 內容驗證
        assertEquals("測試標題 with English & 特殊符號!@#", createdNote.getTitle());
        assertEquals("這是一段較長的測試內容...", createdNote.getContent());
        assertEquals(testUserEmail, createdNote.getUserEmail());

        // 時間戳記
        assertNotNull(createdNote.getCreatedAt());
        assertNotNull(createdNote.getUpdatedAt());
    }

    @Test
    void testGetNotes() {
        // 測試查詢筆記 - 一般使用者查詢自己的筆記
        // 準備測試資料
        Note note1 = new Note();
        note1.setTitle("測試標題1");
        note1.setContent("測試內容1");

        Note note2 = new Note();
        note2.setTitle("測試標題2");
        note2.setContent("測試內容2");

        noteService.createNote(note1, testUserEmail);
        noteService.createNote(note2, testUserEmail);

        // 執行測試
        Pageable pageable = PageRequest.of(0, 10);
        Page<Note> allNotes = noteService.getAllNotes(pageable, testUserEmail, false);

        // 驗證結果
        assertTrue(allNotes.getTotalElements() >= 2);
        allNotes.getContent().forEach(note -> {
            assertEquals(testUserEmail, note.getUserEmail());
            assertNotNull(note.getId());
            assertNotNull(note.getTitle());
            assertNotNull(note.getContent());
        });
    }

    @Test
    void testUpdateNote() {
        // 測試更新筆記
        // 準備測試資料
        Note originalNote = new Note();
        originalNote.setTitle("原始標題");
        originalNote.setContent("原始內容");

        Note saved = noteService.createNote(originalNote, testUserEmail);

        Note newNote = new Note();
        newNote.setTitle("新標題");
        newNote.setContent("新內容");

        // 執行測試
        Note updated = noteService.updateNote(saved.getId(), newNote, testUserEmail, false);

        // 驗證結果
        assertNotNull(updated);
        assertEquals(saved.getId(), updated.getId());
        assertEquals("新標題", updated.getTitle());
        assertEquals("新內容", updated.getContent());
        assertEquals(testUserEmail, updated.getUserEmail());
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(saved.getCreatedAt()));
    }
}