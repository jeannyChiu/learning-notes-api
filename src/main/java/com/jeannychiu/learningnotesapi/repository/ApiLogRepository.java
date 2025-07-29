package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * API 日誌資料存取層
 *
 * 負責 API 請求和回應日誌的資料庫操作。
 * 使用 Spring Data JPA 自動實作基本的 CRUD 操作。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {
}
