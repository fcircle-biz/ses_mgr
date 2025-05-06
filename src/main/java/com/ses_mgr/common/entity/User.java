package com.ses_mgr.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"department", "userRoles"})
@EqualsAndHashCode(exclude = {"department", "userRoles"})
public class User implements UserDetails {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    private String position;

    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "mfa_enabled")
    private Boolean mfaEnabled;

    @Column(nullable = false)
    private String status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_attempts")
    private Integer loginAttempts;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.userId = (this.userId == null) ? UUID.randomUUID() : this.userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = (this.status == null) ? "active" : this.status;
        this.loginAttempts = (this.loginAttempts == null) ? 0 : this.loginAttempts;
        this.mfaEnabled = (this.mfaEnabled == null) ? false : this.mfaEnabled;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // UserDetails 実装メソッド

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (userRoles == null || userRoles.isEmpty()) {
            // デフォルトで USER 権限を付与
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return userRoles.stream()
                .filter(userRole -> userRole.getRole() != null)
                .map(userRole -> {
                    String roleCode = userRole.getRole().getRoleCode();
                    return new SimpleGrantedAuthority("ROLE_" + roleCode);
                })
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"locked".equals(this.status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (this.passwordExpiresAt == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(this.passwordExpiresAt);
    }

    @Override
    public boolean isEnabled() {
        return "active".equals(this.status);
    }

    // ユーティリティメソッド
    public void addRole(Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(this);
        userRole.setRole(role);
        userRole.setAssignedAt(LocalDateTime.now());
        this.userRoles.add(userRole);
    }

    public void removeRole(Role role) {
        this.userRoles.removeIf(userRole -> userRole.getRole().equals(role));
    }

    public boolean hasRole(String roleCode) {
        return this.userRoles.stream()
                .anyMatch(userRole -> userRole.getRole().getRoleCode().equals(roleCode));
    }
}