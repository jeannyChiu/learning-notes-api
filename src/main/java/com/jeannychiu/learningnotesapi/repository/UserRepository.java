package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 使用者資料存取層
 *
 * 負責使用者的資料庫操作。
 * 提供根據信箱查詢使用者的功能。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根據信箱查詢使用者
     *
     * @param email 使用者信箱
     * @return 包含使用者的Optional，如果不存在則為空
     */
    Optional<User> findByEmail(String email);
}
