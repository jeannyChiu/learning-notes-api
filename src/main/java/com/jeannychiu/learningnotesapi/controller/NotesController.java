package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.service.NoteService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 筆記管理控制器
 *
 * 處理筆記的 CRUD（Create, Read, Update, Delete）操作。
 * 所有操作都需要使用者認證，並會根據使用者角色進行權限檢查。
 * 一般使用者只能操作自己的筆記，管理員可以操作所有筆記。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/notes")
public class NotesController {
    private final NoteService noteService;

    public NotesController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 取得所有筆記
     *
     * 根據使用者角色返回不同結果：
     * - 一般使用者：只能看到自己的筆記
     * - 管理員：可以看到所有筆記
     * 支援分頁和搜尋功能。
     *
     * @param page 頁碼，從0開始 (預設值：0)
     * @param size 每頁筆數 (預設值：10)
     * @param search 搜尋關鍵字，可搜尋標題及內容 (選填)
     * @param authentication Spring Security 的認證物件
     * @return 分頁的筆記列表
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<Note> getAllNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (search != null && !search.trim().isEmpty()) {
            return noteService.searchNotes(PageRequest.of(page, size), userEmail, isAdmin, search);
        } else {
            return noteService.getAllNotes(PageRequest.of(page, size), userEmail, isAdmin);
        }
    }

    /**
     * 創建筆記
     *
     * 設置當前使用者為筆記擁有者
     *
     * @param note 筆記物件
     * @param authentication Spring Security 的認證物件
     * @return 創建成功的筆記物件，HTTP 狀態碼 201
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Note> createNote(@RequestBody @Valid Note note, Authentication authentication) {
        String userEmail = authentication.getName();
        Note createdNote = noteService.createNote(note, userEmail);

        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    /**
     * 根據筆記ID讀取筆記
     *
     * 根據使用者角色返回不同結果：
     * - 一般使用者：只能看到自己的筆記
     * - 管理員：可以看到所有筆記
     *
     * @param id 筆記ID
     * @param authentication Spring Security 的認證物件
     * @return 筆記物件，HTTP 狀態碼 200
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        Note note = noteService.readNoteById(id, userEmail, isAdmin);
        
        return ResponseEntity.ok(note);
    }

    /**
     * 更新筆記
     *
     * 根據使用者角色有不同權限：
     * - 一般使用者：只能更新自己的筆記
     * - 管理員：可以更新所有筆記
     *
     * @param id 筆記ID
     * @param noteDetails 筆記物件
     * @param authentication Spring Security 的認證物件
     * @return 更新後的筆記物件，HTTP 狀態碼 200
     */
    @PutMapping("/{id}")
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

    /**
     * 刪除筆記
     *
     * 根據使用者角色有不同權限：
     * - 一般使用者：只能刪除自己的筆記
     * - 管理員：可以刪除所有筆記
     *
     * @param id 筆記ID
     * @param authentication Spring Security 的認證物件
     * @return 無內容，HTTP 狀態碼 204
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        noteService.deleteNote(id, userEmail, isAdmin);
        
        return ResponseEntity.noContent().build();
    }
}
