package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 筆記資料存取層
 *
 * 負責筆記的資料庫操作，包含基本的 CRUD 和自定義查詢。
 * 提供依使用者信箱和關鍵字搜尋筆記的功能。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
public interface NoteRepository extends JpaRepository<Note, Long> {
    /**
     * 根據使用者信箱查詢筆記
     *
     * @param userEmail 使用者信箱
     * @param pageable 分頁參數
     * @return 該使用者的筆記分頁結果
     */
    Page<Note> findByUserEmail(String userEmail, Pageable pageable);

    /**
     * 檢查筆記是否屬於特定使用者
     *
     * @param id 筆記 ID
     * @param userEmail 使用者信箱
     * @return 是否屬於特定使用者
     */
    boolean existsByIdAndUserEmail(Long id, String userEmail);

    /**
     * 根據標題或內容搜尋使用者的筆記
     *
     * @param userEmail 使用者信箱
     * @param keyword 關鍵字
     * @param pageable 分頁參數
     * @return 搜尋的筆記分頁結果
     */
    @Query("SELECT n FROM Note n WHERE n.userEmail = :userEmail AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> findByUserEmailAndKeyword(@Param("userEmail") String userEmail, 
                                       @Param("keyword") String keyword, 
                                       Pageable pageable);

    /**
     * 根據標題或內容搜尋筆記，管理員專用
     *
     * @param keyword 關鍵字
     * @param pageable 分頁參數
     * @return 搜尋的筆記分頁結果
     */
    @Query("SELECT n FROM Note n WHERE " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
