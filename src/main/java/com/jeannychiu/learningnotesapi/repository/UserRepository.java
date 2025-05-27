package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
