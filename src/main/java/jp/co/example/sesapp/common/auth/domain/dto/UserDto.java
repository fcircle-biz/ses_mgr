package jp.co.example.sesapp.common.auth.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ユーザーDTO。
 * クライアントに返すユーザー情報を表します。
 */
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private String department;
    private String position;
    private String phone;
    private List<String> roles;
    private String role;
    private boolean accountLocked;
    private boolean mfaEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String firstName;
    private String lastName;
    private String username;
    private UUID departmentId;
    private boolean enabled;
    private LocalDateTime accountExpireDate;

    public UserDto() {
    }
    
    public UserDto(UUID id, String email, String name, String department, String position, 
                  String phone, String role, LocalDateTime createdAt, LocalDateTime updatedAt, 
                  LocalDateTime lastLoginAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.department = department;
        this.position = position;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getPasswordExpiresAt() {
        return passwordExpiresAt;
    }

    public void setPasswordExpiresAt(LocalDateTime passwordExpiresAt) {
        this.passwordExpiresAt = passwordExpiresAt;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
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
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getAccountExpireDate() {
        return accountExpireDate;
    }

    public void setAccountExpireDate(LocalDateTime accountExpireDate) {
        this.accountExpireDate = accountExpireDate;
    }
    
    public LocalDateTime getCredentialsExpireDate() {
        return passwordExpiresAt;
    }

    public void setCredentialsExpireDate(LocalDateTime credentialsExpireDate) {
        this.passwordExpiresAt = credentialsExpireDate;
    }
    
    public LocalDateTime getLastLoginDate() {
        return lastLoginAt;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginAt = lastLoginDate;
    }
}