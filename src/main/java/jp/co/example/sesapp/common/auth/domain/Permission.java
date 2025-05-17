package jp.co.example.sesapp.common.auth.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 権限エンティティ。
 * 特定の操作や機能へのアクセス権を表します。
 */
public class Permission {
    private UUID id;
    private String name;
    private String description;
    private ResourceType resourceType;
    private String action;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Permission() {
    }

    public Permission(UUID id, String name, String description, ResourceType resourceType, String action) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.resourceType = resourceType;
        this.action = action;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ファクトリーメソッド
    public static Permission createNewPermission(ResourceType resourceType, String action, String description) {
        UUID id = UUID.randomUUID();
        String name = resourceType.name().toLowerCase() + ":" + action.toLowerCase();
        return new Permission(id, name, description, resourceType, action);
    }

    // Getters & Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    
    public ActionType getActionType() {
        return ActionType.valueOf(action);
    }
    
    public void setActionType(ActionType actionType) {
        this.action = actionType.name();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}