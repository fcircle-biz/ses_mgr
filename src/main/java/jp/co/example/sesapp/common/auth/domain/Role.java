package jp.co.example.sesapp.common.auth.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ロールエンティティ。
 * ユーザーに割り当てられる役割を表します。
 */
public class Role {
    private UUID id;
    private String name;
    private String description;
    private boolean systemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Permission> permissions = new HashSet<>();

    public Role() {
    }

    public Role(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ファクトリーメソッド
    public static Role createNewRole(String name, String description) {
        return new Role(UUID.randomUUID(), name, description);
    }

    // ビジネスロジックメソッド
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        this.updatedAt = LocalDateTime.now();
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
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

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }
}