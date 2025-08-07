package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
     * 根據標題或內容搜尋筆記 (管理員專用)
     *
     * @param keyword 關鍵字
     * @param pageable 分頁參數
     * @return 搜尋的筆記分頁結果
     */
    @Query("SELECT n FROM Note n WHERE " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根據標籤名稱搜尋使用者的筆記
     *
     * @param userEmail 使用者信箱
     * @param tagName 標籤名稱
     * @param pageable 分頁參數
     * @return 該使用者包含該標籤的筆記分頁結果
     */
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE n.userEmail = :userEmail AND t.name = :tagName")
    Page<Note> findByUserEmailAndTagName(@Param("userEmail") String userEmail,
                                         @Param("tagName") String tagName,
                                         Pageable pageable);

    /**
     * 根據標籤名稱搜尋筆記 (管理員專用)
     *
     * @param tagName 標籤名稱
     * @param pageable 分頁參數
     * @return 包含該標籤的筆記分頁結果
     */
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name = :tagName")
    Page<Note> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    /**
     * 根據標籤名稱和關鍵字搜尋使用者的筆記
     *
     * @param userEmail 使用者信箱
     * @param tagName 標籤名稱
     * @param keyword 關鍵字
     * @param pageable 分頁參數
     * @return 使用者同時包含該標籤和關鍵字的筆記分頁結果
     */
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE n.userEmail = :userEmail " +
            "AND t.name = :tagName " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> findByUserEmailAndTagNameAndKeyword(@Param("userEmail") String userEmail,
                                                   @Param("tagName") String tagName,
                                                   @Param("keyword") String keyword,
                                                   Pageable pageable);

    /**
     * 根據標籤名稱和關鍵字搜尋使用者的筆記 (管理員專用)
     *
     * @param tagName 標籤名稱
     * @param keyword 關鍵字
     * @param pageable 分頁參數
     * @return 包含該標籤和關鍵字的筆記分頁結果
     */
    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name = :tagName " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> findByTagNameAndKeyword(@Param("tagName") String tagName,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    /**
     * 根據關鍵字搜尋筆記標題並返回搜尋建議
     *
     * 使用 GROUP BY 去除重複的標題，並按更新時間降序排序。
     *
     * @param userEmail 使用者信箱
     * @param keyword 搜尋關鍵字
     * @param limit 返回結果的最大數量
     * @return Object[]陣列列表，每個陣列包含 [id, title, type, matched_text, updated_at]
     */
    @Query(value =
            "  SELECT MIN(n.id), n.title, 'title' as type, n.title as matched_text, MAX(n.updated_at) " +
            "  FROM note n " +
            "  WHERE n.user_email = :userEmail " +
            "  AND LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "  GROUP BY n.title " +
            "  ORDER BY MAX(updated_at) DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findSuggestionsForUser(@Param("userEmail") String userEmail,
                                          @Param("keyword") String keyword,
                                          @Param("limit") int limit);

    /**
     * 根據關鍵字搜尋筆記標題並返回搜尋建議 (管理員專用)
     *
     * @param keyword 搜尋關鍵字
     * @param limit 返回結果的最大數量
     * @return Object[]陣列列表，每個陣列包含 [id, title, type, matched_text, updated_at]
     */
    @Query(value =
            "  SELECT MIN(n.id), n.title, 'title' as type, n.title as matched_text, MAX(n.updated_at) " +
            "  FROM note n " +
            "  WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "  GROUP BY n.title " +
            "  ORDER BY MAX(updated_at) DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findSuggestionsForAdmin(@Param("keyword") String keyword,
                                           @Param("limit") int limit);
}
