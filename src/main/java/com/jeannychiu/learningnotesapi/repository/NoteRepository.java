package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    // 根據用戶 email 查詢筆記
    List<Note> findByUserEmail(String userEmail);

    // 分頁查詢特定用戶的筆記
    Page<Note> findByUserEmail(String userEmail, Pageable pageable);

    // 檢查筆記是否屬於特定用戶
    boolean existsByIdAndUserEmail(Long id, String userEmail);
}
