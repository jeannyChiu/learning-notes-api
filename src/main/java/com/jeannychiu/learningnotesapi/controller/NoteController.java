package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.service.NoteService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class NoteController {
    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // 取得所有筆記 (根據用戶角色返回不同結果)
    // HTTP 方法：GET
    // 路徑：/notes
    // 回傳類型：Page<Note>
    @GetMapping("/notes")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<Note> getAllNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        return noteService.getAllNotes(PageRequest.of(page, size), userEmail, isAdmin);
    }

    // 創建筆記 (設置當前用戶為筆記擁有者)
    // HTTP 方法：POST
    // 路徑：/notes
    // 回傳類型：ResponseEntity<Note>
    @PostMapping("/notes")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Note> createNote(@RequestBody @Valid Note note, Authentication authentication) {
        String userEmail = authentication.getName();
        Note createdNote = noteService.createNote(note, userEmail);

        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    // 根據 ID 讀取筆記 (檢查權限)
    // HTTP 方法：GET
    // 路徑：/notes/{id}
    // 回傳類型：ResponseEntity<Note>
    @GetMapping("/notes/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        Note note = noteService.readNoteById(id, userEmail, isAdmin);
        
        return ResponseEntity.ok(note);
    }

    // 更新筆記 (檢查權限)
    // HTTP 方法：PUT
    // 路徑：/notes/{id}
    // 回傳類型：ResponseEntity<Note>
    @PutMapping("/notes/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id, 
            @RequestBody @Valid Note noteDetails,
            Authentication authentication) {

        String userEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Note updatedNote = noteService.updateNote(id, noteDetails, userEmail, isAdmin);
        
        return ResponseEntity.ok(updatedNote);
    }

    // 刪除筆記 (檢查權限)
    // HTTP 方法：DELETE
    // 路徑：/notes/{id}
    // 回傳類型：ResponseEntity<Void>
    @DeleteMapping("/notes/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        noteService.deleteNote(id, userEmail, isAdmin);
        
        return ResponseEntity.noContent().build();
    }
}
