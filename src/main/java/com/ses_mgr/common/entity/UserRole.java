package com.ses_mgr.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "role", "assignedBy"})
@EqualsAndHashCode(exclude = {"user", "role", "assignedBy"})
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            UUID userId = (user != null) ? user.getUserId() : null;
            UUID roleId = (role != null) ? role.getRoleId() : null;
            
            // 少なくともuserIdはnullでないことを確認
            if (userId != null) {
                this.id = new UserRoleId(userId, roleId);
            } else {
                // ユーザーIDがない場合はエラー
                throw new IllegalStateException("UserRole must have a non-null User with userId");
            }
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.assignedAt = (this.assignedAt == null) ? LocalDateTime.now() : this.assignedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 有効期限切れかどうかを確認
    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}