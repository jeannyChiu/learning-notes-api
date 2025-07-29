package com.jeannychiu.learningnotesapi.service;

import com.jeannychiu.learningnotesapi.exception.NoteNotFoundException;
import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.repository.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * 筆記服務層
 *
 * 處理筆記相關的業務邏輯，包含權限檢查、資料驗證等。
 * 實作了基於角色的存取控制（RBAC）：
 * - 一般使用者只能存取自己的筆記
 * - 管理員可以存取所有筆記
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
@Service
public class NoteService {
    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * 創建筆記
     *
     * 設置當前使用者為筆記擁有者，並自動設定建立和更新時間。
     *
     * @param note 要創建的筆記
     * @param userEmail 使用者信箱
     * @return 創建成功的筆記
     */
    public Note createNote(Note note, String userEmail) {
        note.setUserEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        return noteRepository.save(note);
    }

    /**
     * 檢查使用者是否為筆記擁有者
     *
     * @param noteId 筆記 ID
     * @param userEmail 使用者信箱
     * @return 是否為筆記擁有者
     */
    public boolean isNoteOwner(Long noteId, String userEmail) {
        return noteRepository.existsByIdAndUserEmail(noteId, userEmail);
    }

    /**
     * 檢查使用者是否有權限操作筆記
     *
     * @param noteId 筆記 ID
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     * @return 是否有權限操作筆記
     */
    public boolean hasNotePermission(Long noteId, String userEmail, boolean isAdmin) {
        return isAdmin || isNoteOwner(noteId, userEmail);
    }

    /**
     * 根據筆記 ID 查看筆記
     *
     * @param id 筆記 ID
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     * @return 查看的筆記
     */
    public Note readNoteById(Long id, String userEmail, boolean isAdmin) {
        // 先檢查筆記是否存在
        Note note = findNoteById(id);
        
        // 檢查權限
        if (!hasNotePermission(id, userEmail, isAdmin)) { 
            throw new AccessDeniedException("您沒有權限查看此筆記");
        }
        
        return note;
    }

    /**
     * 查看所有筆記
     *
     * - 一般使用者只能查看自己的筆記
     * - 管理員可查看所有筆記
     *
     * @param pageable 分頁筆記
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     * @return 分頁的筆記列表
     */
    public Page<Note> getAllNotes(Pageable pageable, String userEmail, boolean isAdmin) {
        if (isAdmin) {
            // 管理員可以查看所有筆記
            return noteRepository.findAll(pageable);
        } else {
            // 一般使用者只能查看自己的筆記
            return noteRepository.findByUserEmail(userEmail, pageable);
        }
    }

    /**
     * 搜尋筆記
     *
     * 依據關鍵字查詢筆記
     *
     *  - 一般使用者只能搜尋自己的筆記
     *  - 管理員可搜尋所有筆記
     *
     * @param pageable 分頁筆記
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     * @param keyword 關鍵字
     * @return 分頁的筆記列表
     */
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

    /**
     * 更新筆記
     *
     * 更新該 ID 筆記，並自動設定更新時間。
     *
     * - 一般使用者只能更新自己的筆記
     * - 管理員可更新所有筆記
     *
     * @param id 筆記 ID
     * @param noteDetails 更新的筆記物件
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     * @return 更新的筆記
     */
    public Note updateNote(Long id, Note noteDetails, String userEmail, boolean isAdmin) {
        // 先檢查筆記是否存在
        Note note = findNoteById(id);

        // 如果不是管理員，且不是筆記擁有者，拒絕更新
        if (!hasNotePermission(id, userEmail, isAdmin)) { 
            throw new AccessDeniedException("您沒有權限更新此筆記");
        }   

        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        note.setUpdatedAt(LocalDateTime.now());
        
        return noteRepository.save(note);
    }

    /**
     * 刪除筆記
     *
     * - 一般使用者只能刪除自己的筆記
     * - 管理員可刪除所有筆記
     *
     * @param id 筆記 ID
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     */
    public void deleteNote(Long id, String userEmail, boolean isAdmin) {
        // 先檢查筆記是否存在
        Note note = findNoteById(id);

        // 如果不是管理員，且不是筆記擁有者，拒絕刪除
        if (!hasNotePermission(id, userEmail, isAdmin)) { 
            throw new AccessDeniedException("您沒有權限刪除此筆記");
        }   
        
        noteRepository.deleteById(id);
    }

    /**
     * 根據筆記 ID 查詢筆記
     *
     * @param id 筆記ID
     * @return 筆記物件
     * @throws NoteNotFoundException 當筆記不存在時
     */
    private Note findNoteById(Long id){
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("找不到 ID 為 " + id + " 的筆記"));
    }
}