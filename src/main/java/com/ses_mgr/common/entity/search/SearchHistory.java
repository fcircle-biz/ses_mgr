package com.ses_mgr.common.entity.search;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 検索履歴エンティティ
 * ユーザーが行った検索の履歴を保持します
 */
@Entity
@Table(name = "search_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "search_id", columnDefinition = "BINARY(16)")
    private UUID searchId;

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Column(name = "query", nullable = false)
    private String query;

    @Column(name = "resource_types", columnDefinition = "JSON")
    private String resourceTypes;

    @Column(name = "filters", columnDefinition = "JSON")
    private String filters;

    @Column(name = "sort_field")
    private String sortField;

    @Column(name = "sort_order")
    private String sortOrder;

    @Column(name = "group_by_resource_type")
    private Boolean groupByResourceType;

    @Column(name = "result_count")
    private Integer resultCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (resultCount == null) {
            resultCount = 0;
        }
        if (groupByResourceType == null) {
            groupByResourceType = false;
        }
    }
}