package com.ses_mgr.common.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ロールモデル
 */
public class Role {
    private String roleId;
    private String roleCode;
    private String name;
    private String description;
    private String roleType; // system or business
    private List<Permission> permissions;
    private Integer userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public String getRoleId() {
        return roleId;
    }
    
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
    
    public String getRoleCode() {
        return roleCode;
    }
    
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
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
    
    public String getRoleType() {
        return roleType;
    }
    
    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }
    
    public List<Permission> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
    
    public Integer getUserCount() {
        return userCount;
    }
    
    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
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
}
