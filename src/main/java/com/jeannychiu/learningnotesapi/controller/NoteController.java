package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NoteController {
    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // 取得所有筆記
    // HTTP 方法：GET
    // 路徑：/notes
    // 回傳類型：List<Note>
    @GetMapping("/notes")
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    // 創建筆記
    // HTTP 方法：POST
    // 路徑：/notes
    // 回傳類型：Note
    @PostMapping("/notes")
    public Note createNote(@RequestBody Note note) {
        return noteService.createNote(note);
    }

    // 取得特定ID筆記
    // HTTP 方法：GET
    // 路徑：/notes/{id}
    // 回傳類型：Note
    @GetMapping("/notes/{id}")
    public Note readNoteById(@PathVariable Long id) {
        return noteService.readNoteById(id);
    }

    // 更新特定ID筆記
    // HTTP 方法：PUT
    // 路徑：/notes/{id}
    // 回傳類型：Note
    @PutMapping("/notes/{id}")
    public Note updateNote(@PathVariable Long id, @RequestBody Note note) {
        return noteService.updateNote(id, note);
    }

    // 刪除特定ID筆記
    // HTTP 方法：DELETE
    // 路徑：/notes/{id}
    // 回傳類型：Note
    @DeleteMapping("/notes/{id}")
    public void deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
    }


}
