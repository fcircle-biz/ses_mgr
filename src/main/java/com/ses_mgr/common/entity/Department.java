package com.ses_mgr.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "department_code", unique = true)
    private String departmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @OneToMany(mappedBy = "parentDepartment", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Department> childDepartments = new HashSet<>();

    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "department")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = (this.isActive == null) ? true : this.isActive;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 子部署の追加
    public void addChildDepartment(Department child) {
        childDepartments.add(child);
        child.setParentDepartment(this);
    }

    // 子部署の削除
    public void removeChildDepartment(Department child) {
        childDepartments.remove(child);
        child.setParentDepartment(null);
    }

    // ユーザーの追加
    public void addUser(User user) {
        users.add(user);
        user.setDepartment(this);
    }

    // ユーザーの削除
    public void removeUser(User user) {
        users.remove(user);
        user.setDepartment(null);
    }
}