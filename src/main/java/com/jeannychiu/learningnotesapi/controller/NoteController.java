package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.service.NoteService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    // 回傳類型：Page<Note>
    @GetMapping("/notes")
    public Page<Note> getAllNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return noteService.getAllNotes(PageRequest.of(page, size));
    }

    // 創建筆記
    // HTTP 方法：POST
    // 路徑：/notes
    // 回傳類型：ResponseEntity<Note>
    @PostMapping("/notes")
    public ResponseEntity<Note> createNote(@RequestBody @Valid Note note) {
        Note createdNote = noteService.createNote(note);
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    // 取得特定ID筆記
    // HTTP 方法：GET
    // 路徑：/notes/{id}
    // 回傳類型：ResponseEntity<Note>
    @GetMapping("/notes/{id}")
    public ResponseEntity<Note> readNoteById(@PathVariable Long id) {
        Note note = noteService.readNoteById(id);
        if (note != null) {
            return ResponseEntity.ok(note);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 更新特定ID筆記
    // HTTP 方法：PUT
    // 路徑：/notes/{id}
    // 回傳類型：ResponseEntity<String>
    @PutMapping("/notes/{id}")
    public ResponseEntity<String> updateNote(@PathVariable Long id, @RequestBody @Valid Note note) {
        Note updatedNote = noteService.updateNote(id, note);
        if (updatedNote != null) {
            return ResponseEntity.ok("ID 為 " + id + " 的筆記已更新成功");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID 為 " + id + " 的筆記不存在");
        }
    }

    // 刪除特定ID筆記
    // HTTP 方法：DELETE
    // 路徑：/notes/{id}
    // 回傳類型：ResponseEntity<String>
    @DeleteMapping("/notes/{id}")
    public ResponseEntity<String> deleteNote(@PathVariable Long id) {
        boolean deleted = noteService.deleteNote(id);
        if (deleted) {
            return ResponseEntity.ok("ID 為 " + id + " 的筆記已刪除成功");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID 為 " + id + " 的筆記不存在");
        }
    }
}
