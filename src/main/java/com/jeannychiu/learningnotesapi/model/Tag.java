package com.jeannychiu.learningnotesapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(exclude = "notes")
@ToString(exclude = "notes")
public class Tag {
    // 主鍵
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 標籤名稱 (唯一)
    @Column(unique = true, nullable = false)
    private String name;

    // 與 Note 的多對多關係
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Note> notes = new HashSet<>();

    // 時間戳記
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
