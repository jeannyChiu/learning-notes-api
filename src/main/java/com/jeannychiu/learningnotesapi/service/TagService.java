package com.jeannychiu.learningnotesapi.service;

import com.jeannychiu.learningnotesapi.model.Tag;
import com.jeannychiu.learningnotesapi.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 標籤服務層
 *
 * 處理標籤相關的業務邏輯
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * 建立新標籤或返回已存在的標籤
     *
     * @param name 標籤名稱
     * @return 標籤物件
     */
    @Transactional
    public Tag createOrGetTag(String name) {
        // 輸入檢查
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("標籤名稱不能為空");
        }

        // 去除空白
        String cleanName = name.trim();

        // 檢查是否已存在
        boolean isExist = tagRepository.existsByName(cleanName);

        // 如果存在，返回現有標籤
        if (isExist) {
            return tagRepository.findByName(cleanName).get();
        } else {
            // 如果不存在，建立新標籤
            Tag tag = new Tag();
            tag.setName(cleanName);
            LocalDateTime now = LocalDateTime.now();
            tag.setCreatedAt(now);
            tag.setUpdatedAt(now);

            return tagRepository.save(tag);
        }
    }

    /**
     * 批次處理標籤，返回標籤集合
     *
     * @param tagNames 標籤名稱集合
     * @return 標籤集合
     */
    @Transactional
    public Set<Tag> createOrGetTags(Set<String> tagNames) {
        // 輸入處理
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> validTagNames = tagNames.stream()
                .filter(name -> name != null)
                .filter(name -> !name.trim().isEmpty())
                .collect(Collectors.toSet());

        if (validTagNames.isEmpty()) {
            return new HashSet<>();
        }

        // 查詢現有標籤
        List<String> tagNameList = new ArrayList<>(validTagNames);
        List<Tag> existTagList = tagRepository.findByNameIn(tagNameList);
        Set<String> existTagName = new HashSet<>();
        existTagList.forEach(existTag -> existTagName.add(existTag.getName()));

        // 找出需要建立的標籤
        Set<String> createTagName = new HashSet<>(validTagNames);
        createTagName.removeAll(existTagName);

        // 建立新標籤
        List<Tag> newTags = new ArrayList<>();

        createTagName.forEach(tagName -> {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            LocalDateTime now = LocalDateTime.now();
            newTag.setCreatedAt(now);
            newTag.setUpdatedAt(now);

            newTags.add(newTag);
        });

        List<Tag> savedTags = tagRepository.saveAll(newTags);

        // 合併結果
        Set<Tag> resultTags = new HashSet<>();
        resultTags.addAll(existTagList);
        resultTags.addAll(savedTags);

        return resultTags;
    }
}