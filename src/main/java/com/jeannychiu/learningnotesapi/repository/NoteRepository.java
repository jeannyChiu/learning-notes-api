package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    // 根據用戶 email 查詢筆記
    List<Note> findByUserEmail(String userEmail);

    // 分頁查詢特定用戶的筆記
    Page<Note> findByUserEmail(String userEmail, Pageable pageable);

    // 檢查筆記是否屬於特定用戶
    boolean existsByIdAndUserEmail(Long id, String userEmail);
    
    // 搜尋功能：根據標題或內容搜尋特定用戶的筆記
    @Query("SELECT n FROM Note n WHERE n.userEmail = :userEmail AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> findByUserEmailAndKeyword(@Param("userEmail") String userEmail, 
                                       @Param("keyword") String keyword, 
                                       Pageable pageable);
    
    // 管理員搜尋功能：搜尋所有筆記
    @Query("SELECT n FROM Note n WHERE " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
