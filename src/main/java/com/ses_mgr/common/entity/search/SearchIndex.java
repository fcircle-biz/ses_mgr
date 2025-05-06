package com.ses_mgr.common.entity.search;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 検索インデックスエンティティ
 * 全文検索のためのインデックス情報を保持します
 */
@Entity
@Table(name = "search_index")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "index_id")
    private Long indexId;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "attributes", columnDefinition = "JSON")
    private String attributes;

    @Column(name = "url")
    private String url;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "access_roles", columnDefinition = "JSON")
    private String accessRoles;

    @Column(name = "created_by", columnDefinition = "UUID")
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (isPublic == null) {
            isPublic = false;
        }
    }
}