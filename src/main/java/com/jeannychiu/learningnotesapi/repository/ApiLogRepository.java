package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {
}
