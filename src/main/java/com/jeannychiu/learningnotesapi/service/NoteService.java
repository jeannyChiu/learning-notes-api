package com.jeannychiu.learningnotesapi.service;

import com.jeannychiu.learningnotesapi.exception.NoteNotFoundException;
import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.repository.NoteRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteService {
    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    // 創建筆記 (設置當前用戶為筆記擁有者)
    public Note createNote(Note note, String userEmail) {
        note.setUserEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        return noteRepository.save(note);
    }

    // 檢查用戶是否為筆記擁有者
    public boolean isNoteOwner(Long noteId, String userEmail) {
        return noteRepository.existsByIdAndUserEmail(noteId, userEmail);
    }

    // 檢查用戶是否有權限操作筆記
    public boolean hasNotePermission(Long noteId, String userEmail, boolean isAdmin) {
        return isAdmin || isNoteOwner(noteId, userEmail);
    }

    // 根據 ID 讀取筆記 (檢查權限)
    public Note readNoteById(Long id, String userEmail, boolean isAdmin) {
        // 先檢查筆記是否存在
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("找不到 ID 為 " + id + " 的筆記"));
        
        // 檢查權限
        if (!hasNotePermission(id, userEmail, isAdmin)) { 
            throw new AccessDeniedException("您沒有權限查看此筆記");
        }
        
        return note;
    }

    // 取得所有筆記 (根據用戶角色返回不同結果)
    public Page<Note> getAllNotes(Pageable pageable, String userEmail, boolean isAdmin) {
        if (isAdmin) {
            // 管理員可以查看所有筆記
            return noteRepository.findAll(pageable);
        } else {
            // 一般使用者只能查看自己的筆記
            return noteRepository.findByUserEmail(userEmail, pageable);
        }
    }
    
    // 搜尋筆記 (根據用戶角色返回不同結果)
    public Page<Note> searchNotes(Pageable pageable, String userEmail, boolean isAdmin, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllNotes(pageable, userEmail, isAdmin);
        }
        
        if (isAdmin) {
            // 管理員可以搜尋所有筆記
            return noteRepository.findByKeyword(keyword.trim(), pageable);
        } else {
            // 一般使用者只能搜尋自己的筆記
            return noteRepository.findByUserEmailAndKeyword(userEmail, keyword.trim(), pageable);
        }
    }

    // 更新筆記 (檢查權限)
    public Note updateNote(Long id, Note noteDetails, String userEmail, boolean isAdmin) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("找不到 ID 為 " + id + " 的筆記"));

        // 如果不是管理員，且不是筆記擁有者，拒絕更新
        if (!hasNotePermission(id, userEmail, isAdmin)) { 
            throw new AccessDeniedException("您沒有權限更新此筆記");
        }   

        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        note.setUpdatedAt(LocalDateTime.now());
        
        return noteRepository.save(note);
    }

    // 刪除筆記 (檢查權限)
    public void deleteNote(Long id, String userEmail, boolean isAdmin) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("找不到 ID 為 " + id + " 的筆記"));

        // 如果不是管理員，且不是筆記擁有者，拒絕刪除
        if (!hasNotePermission(id, userEmail, isAdmin)) { 
            throw new AccessDeniedException("您沒有權限刪除此筆記");
        }   
        
        noteRepository.deleteById(id);
    }
}
