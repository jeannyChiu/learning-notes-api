package com.jeannychiu.learningnotesapi.service;

import com.jeannychiu.learningnotesapi.dto.CreateNoteRequest;
import com.jeannychiu.learningnotesapi.dto.UpdateNoteRequest;
import com.jeannychiu.learningnotesapi.exception.NoteNotFoundException;
import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.model.Tag;
import com.jeannychiu.learningnotesapi.repository.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TagService tagService;

    public NoteService(NoteRepository noteRepository, TagService tagService) {
        this.noteRepository = noteRepository;
        this.tagService = tagService;
    }

    /**
     * 創建筆記
     *
     * 設置當前使用者為筆記擁有者，並自動設定建立和更新時間。
     *
     * @param createNoteRequest 要創建的筆記請求物件
     * @param userEmail 使用者信箱
     * @return 創建成功的筆記
     */
    public Note createNote(CreateNoteRequest createNoteRequest, String userEmail) {
        // 建立 Note 物件
        Note note = new Note();
        note.setTitle(createNoteRequest.getTitle());
        note.setContent(createNoteRequest.getContent());
        note.setUserEmail(userEmail);

        // 處理標籤
        Set<String> createNoteTags = createNoteRequest.getTagNames();
        Set<Tag> noteTags = tagService.createOrGetTags(createNoteTags);
        note.setTags(noteTags);

        // 設定時間戳記
        LocalDateTime now = LocalDateTime.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);

        // 保存並返回
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
        // 分頁的筆記列表以更新時間最新排序
        PageRequest sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        if (isAdmin) {
            // 管理員可以查看所有筆記
            return noteRepository.findAll(sortedPageable);
        } else {
            // 一般使用者只能查看自己的筆記
            return noteRepository.findByUserEmail(userEmail, sortedPageable);
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
        // 分頁的筆記列表以更新時間最新排序
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllNotes(sortedPageable, userEmail, isAdmin);
        }
        
        if (isAdmin) {
            // 管理員可以搜尋所有筆記
            return noteRepository.findByKeyword(keyword.trim(), sortedPageable);
        } else {
            // 一般使用者只能搜尋自己的筆記
            return noteRepository.findByUserEmailAndKeyword(userEmail, keyword.trim(), sortedPageable);
        }
    }

    /**
     * 根據標籤搜尋筆記
     *
     * - 一般使用者只能搜尋自己的筆記
     * - 管理員可搜尋所有筆記
     *
     * @param pageable 分頁參數
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     * @param tagName 標籤名稱
     * @return 分頁的筆記列表
     */
    public Page<Note> searchNotesByTag(Pageable pageable, String userEmail, boolean isAdmin, String tagName) {
        // 分頁的筆記列表以更新時間最新排序
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        if (tagName == null || tagName.trim().isEmpty()) {
            return Page.empty(sortedPageable);
        }

        if (isAdmin) {
            // 管理員可以搜尋所有筆記
            return noteRepository.findByTagName(tagName.trim(), sortedPageable);
        } else {
            // 一般使用者只能搜尋自己的筆記
            return noteRepository.findByUserEmailAndTagName(userEmail, tagName.trim(), sortedPageable);
        }
    }

    /**
     * 根據標籤名稱和關鍵字搜尋筆記
     *
     * - 一般使用者只能搜尋自己的筆記
     * - 管理員可搜尋所有筆記
     *
     * @param pageable 分頁參數
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     * @param tagName 標籤名稱
     * @param keyword 關鍵字
     * @return 分頁的筆記列表
     */
    public Page<Note> searchNotesByTagAndKeyword(Pageable pageable, String userEmail,
                                                 boolean isAdmin, String tagName, String keyword) {
        // 分頁的筆記列表以更新時間最新排序
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        if (tagName == null || tagName.trim().isEmpty() ||
            keyword == null || keyword.trim().isEmpty()) {
            return Page.empty(sortedPageable);
        }

        if (isAdmin) {
            // 管理員可以搜尋所有筆記
            return noteRepository.findByTagNameAndKeyword(tagName.trim(), keyword.trim(), sortedPageable);
        } else {
            // 一般使用者只能搜尋自己的筆記
            return noteRepository.findByUserEmailAndTagNameAndKeyword(userEmail, tagName.trim(), keyword.trim(), sortedPageable);
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
     * @param updateNoteRequest 更新的筆記請求物件
     * @param userEmail 使用者信箱
     * @param isAdmin 是否為管理員
     * @return 更新的筆記
     */
    @Transactional
    public Note updateNote(Long id, UpdateNoteRequest updateNoteRequest, String userEmail, boolean isAdmin) {
        // 先檢查筆記是否存在
        Note note = findNoteById(id);

        // 如果不是管理員，且不是筆記擁有者，拒絕更新
        if (!hasNotePermission(id, userEmail, isAdmin)) { 
            throw new AccessDeniedException("您沒有權限更新此筆記");
        }

        // 更新筆記資料
        note.setTitle(updateNoteRequest.getTitle());
        note.setContent(updateNoteRequest.getContent());

        // 處理標籤
        Set<String> updateNoteTags = updateNoteRequest.getTagNames();
        Set<Tag> noteTags = tagService.createOrGetTags(updateNoteTags);
        note.setTags(noteTags);

        // 更新時間戳記
        note.setUpdatedAt(LocalDateTime.now());

        // 保存
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
    @Transactional
    public void deleteNote(Long id, String userEmail, boolean isAdmin) {
        // 先檢查筆記是否存在
        findNoteById(id);

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