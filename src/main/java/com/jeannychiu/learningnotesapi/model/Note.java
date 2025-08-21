package com.jeannychiu.learningnotesapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "note")
@Data
@EqualsAndHashCode(exclude = "tags")
@ToString(exclude = "tags")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "標題不能為空")
    private String title;

    @Size(max = 500, message = "內容不能超過 500 字")
    private String content;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 添加用戶 email 字段，用於關聯筆記與用戶
    @Column(name = "user_email")
    private String userEmail;

    @Version
    private Integer version;

    // 加入 tags 屬性
    @ManyToMany
    @JoinTable(
            name = "note_tag",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
