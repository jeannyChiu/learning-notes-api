package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 標籤資料存取層
 *
 * 負責標籤的資料庫操作，包含基本的 CRUD 和自定義查詢。
 * 提供標籤的精確查詢和模糊搜尋功能。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
public interface TagRepository extends JpaRepository<Tag, Long> {
    /**
     * 根據名稱精確查詢標籤
     *
     * @param name 標籤名稱
     * @return 符合的標籤 (Optional)
     */
    Optional<Tag> findByName(String name);

    /**
     * 根據多個名稱查詢標籤
     *
     * @param names 標籤名稱列表
     * @return 符合的標籤列表
     */
    List<Tag> findByNameIn(List<String> names);

    /**
     * 檢查標籤名稱是否已存在
     *
     * @param name 標籤名稱
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 模糊搜尋標籤 (不區分大小寫)
     *
     * @param keyword 搜尋關鍵字
     * @return 符合的標籤列表
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Tag> searchByName(@Param("keyword") String keyword);

    /**
     * 模糊搜尋標籤，支援分頁 (不區分大小寫)
     *
     * @param keyword 搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合的標籤分頁結果
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Tag> searchByName(@Param("keyword") String keyword, Pageable pageable);
}
