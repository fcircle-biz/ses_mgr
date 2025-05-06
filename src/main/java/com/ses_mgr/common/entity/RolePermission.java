package com.ses_mgr.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"role", "permission"})
@EqualsAndHashCode(exclude = {"role", "permission"})
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @Column(name = "access_level")
    private String accessLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null && role != null && permission != null) {
            this.id = new RolePermissionId(role.getRoleId(), permission.getPermissionId());
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.accessLevel = (this.accessLevel == null) ? "none" : this.accessLevel;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // アクセスレベルの確認
    public boolean hasAccess(String requiredLevel) {
        if ("none".equals(this.accessLevel)) {
            return false;
        }
        if ("write".equals(this.accessLevel)) {
            return true; // write は read も含む
        }
        return "read".equals(this.accessLevel) && "read".equals(requiredLevel);
    }
}