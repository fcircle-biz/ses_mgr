package com.ses_mgr.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"userRoles", "rolePermissions"})
@EqualsAndHashCode(exclude = {"userRoles", "rolePermissions"})
public class Role {

    @Id
    @Column(name = "role_id")
    private java.util.UUID roleId;

    @Column(name = "role_code", unique = true, nullable = false)
    private String roleCode;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "role_type")
    private String roleType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RolePermission> rolePermissions = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.roleId = (this.roleId == null) ? java.util.UUID.randomUUID() : this.roleId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.roleType = (this.roleType == null) ? "business" : this.roleType;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 権限の追加
    public void addPermission(Permission permission) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(this);
        rolePermission.setPermission(permission);
        rolePermission.setAccessLevel("read");
        this.rolePermissions.add(rolePermission);
    }

    // 権限の追加（アクセスレベル指定）
    public void addPermission(Permission permission, String accessLevel) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(this);
        rolePermission.setPermission(permission);
        rolePermission.setAccessLevel(accessLevel);
        this.rolePermissions.add(rolePermission);
    }

    // 権限の削除
    public void removePermission(Permission permission) {
        this.rolePermissions.removeIf(rolePermission -> rolePermission.getPermission().equals(permission));
    }

    // 権限の確認
    public boolean hasPermission(String permissionCode) {
        return this.rolePermissions.stream()
                .anyMatch(rp -> rp.getPermission().getPermissionCode().equals(permissionCode) 
                        && !"none".equals(rp.getAccessLevel()));
    }

    // 権限のアクセスレベル確認
    public boolean hasPermission(String permissionCode, String accessLevel) {
        return this.rolePermissions.stream()
                .anyMatch(rp -> rp.getPermission().getPermissionCode().equals(permissionCode) 
                        && accessLevel.equals(rp.getAccessLevel()));
    }
}