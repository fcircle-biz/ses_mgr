package com.ses_mgr.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "permission_code", unique = true, nullable = false)
    private String permissionCode;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_name", nullable = false)
    private String resourceName;

    @Column(nullable = false)
    private String action;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RolePermission> rolePermissions = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.permissionId = (this.permissionId == null) ? UUID.randomUUID() : this.permissionId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ロールの追加
    public void addRole(Role role) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setPermission(this);
        rolePermission.setRole(role);
        rolePermission.setAccessLevel("read");
        this.rolePermissions.add(rolePermission);
    }

    // ロールの追加（アクセスレベル指定）
    public void addRole(Role role, String accessLevel) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setPermission(this);
        rolePermission.setRole(role);
        rolePermission.setAccessLevel(accessLevel);
        this.rolePermissions.add(rolePermission);
    }

    // ロールの削除
    public void removeRole(Role role) {
        this.rolePermissions.removeIf(rolePermission -> rolePermission.getRole().equals(role));
    }

    // ユーティリティメソッド：権限コードの生成
    public static String generatePermissionCode(String resourceType, String resourceName, String action) {
        return resourceType + "." + resourceName + "." + action;
    }
}