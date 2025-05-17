package jp.co.example.sesapp.common.auth.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import jp.co.example.sesapp.common.auth.domain.AuthenticationMethod;

/**
 * ユーザーエンティティ。
 * システムにアクセスする個人を表します。
 */
public class User {
    // aliased fields for backward compatibility
    private UUID id;
    private String email;
    private String passwordHash;
    private String username;
    private String firstName;
    private String lastName;
    private String department;
    private String position;
    private String phone;
    private UUID roleId;
    private UUID departmentId;
    private boolean enabled;
    private int loginFailCount;
    private AuthenticationMethod authenticationMethod;
    private LocalDateTime accountExpireDate;
    private LocalDateTime credentialsExpireDate;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastLoginAt;
    private boolean mfaEnabled;
    private String mfaSecret;
    private boolean accountLocked;
    private LocalDateTime accountExpiresAt;
    private LocalDateTime passwordExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(UUID id, String email, String passwordHash, String username, String firstName, String lastName, 
               String department, String position, String phone, UUID roleId, LocalDateTime passwordExpiresAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.position = position;
        this.phone = phone;
        this.roleId = roleId;
        this.mfaEnabled = false;
        this.accountLocked = false;
        this.passwordExpiresAt = passwordExpiresAt;
        this.credentialsExpireDate = passwordExpiresAt;
        this.enabled = true;
        this.loginFailCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ファクトリーメソッド
    public static User createNewUser(String email, String encodedPassword, String username, String firstName, String lastName,
                                    String department, String position, String phone, UUID roleId) {
        UUID id = UUID.randomUUID();
        LocalDateTime passwordExpiry = LocalDateTime.now().plusDays(90); // 90日間有効
        return new User(id, email, encodedPassword, username, firstName, lastName, department, position, phone, roleId, passwordExpiry);
    }

    // ビジネスロジックメソッド
    public boolean isCredentialsNonExpired() {
        return passwordExpiresAt == null || passwordExpiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isAccountNonExpired() {
        return accountExpiresAt == null || accountExpiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    public boolean isEnabled() {
        return enabled && isAccountNonExpired() && isAccountNonLocked();
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPassword(String encodedPassword) {
        this.passwordHash = encodedPassword;
        this.passwordExpiresAt = LocalDateTime.now().plusDays(90); // 90日間有効
        this.updatedAt = LocalDateTime.now();
    }
    
    public void lockAccount() {
        this.accountLocked = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void unlockAccount() {
        this.accountLocked = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementLoginFailCount() {
        this.loginFailCount++;
        this.updatedAt = LocalDateTime.now();
        // ログイン失敗回数が一定回数を超えたらアカウントをロック
        if (this.loginFailCount >= 5) {
            this.accountLocked = true;
        }
    }
    
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void enableMfa(String secret) {
        this.mfaEnabled = true;
        this.mfaSecret = secret;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void disableMfa() {
        this.mfaEnabled = false;
        this.mfaSecret = null;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
    
    public String getName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }
    
    public void setName(String name) {
        // Split the name into first and last name if it contains a space
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            this.firstName = parts[0];
            this.lastName = parts[1];
        } else {
            this.firstName = name;
        }
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String getMfaSecret() {
        return mfaSecret;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getAccountExpiresAt() {
        return accountExpiresAt;
    }

    public void setAccountExpiresAt(LocalDateTime accountExpiresAt) {
        this.accountExpiresAt = accountExpiresAt;
    }

    public LocalDateTime getPasswordExpiresAt() {
        return passwordExpiresAt;
    }

    public void setPasswordExpiresAt(LocalDateTime passwordExpiresAt) {
        this.passwordExpiresAt = passwordExpiresAt;
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

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }
    
    public int getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(int loginFailCount) {
        this.loginFailCount = loginFailCount;
    }
    
    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
    
    public LocalDateTime getCredentialsExpireDate() {
        return passwordExpiresAt;
    }

    public void setCredentialsExpireDate(LocalDateTime credentialsExpireDate) {
        this.passwordExpiresAt = credentialsExpireDate;
        this.credentialsExpireDate = credentialsExpireDate;
    }
    
    public LocalDateTime getAccountExpireDate() {
        return accountExpiresAt;
    }

    public void setAccountExpireDate(LocalDateTime accountExpireDate) {
        this.accountExpiresAt = accountExpireDate;
        this.accountExpireDate = accountExpireDate;
    }
    
    public LocalDateTime getLastLoginDate() {
        return lastLoginAt;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginAt = lastLoginDate;
        this.lastLoginDate = lastLoginDate;
    }
    
    /**
     * ユーザーオブジェクトのビルダーを作成します。
     * @return ユーザービルダー
     */
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    /**
     * Userオブジェクトを構築するためのビルダークラス
     */
    public static class UserBuilder {
        private UUID id;
        private String email;
        private String passwordHash;
        private String username;
        private String firstName;
        private String lastName;
        private String department;
        private String position;
        private String phone;
        private UUID roleId;
        private UUID departmentId;
        private boolean enabled = true;
        private int loginFailCount = 0;
        private AuthenticationMethod authenticationMethod;
        private LocalDateTime accountExpireDate;
        private LocalDateTime credentialsExpireDate;
        private LocalDateTime lastLoginDate;
        private boolean mfaEnabled = false;
        private String mfaSecret;
        private boolean accountLocked = false;
        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        
        public UserBuilder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }
        
        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder name(String name) {
            if (name != null && name.contains(" ")) {
                String[] parts = name.split(" ", 2);
                this.firstName = parts[0];
                this.lastName = parts[1];
            } else {
                this.firstName = name;
            }
            return this;
        }
        
        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public UserBuilder department(String department) {
            this.department = department;
            return this;
        }
        
        public UserBuilder position(String position) {
            this.position = position;
            return this;
        }
        
        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public UserBuilder roleId(UUID roleId) {
            this.roleId = roleId;
            return this;
        }
        
        public UserBuilder departmentId(UUID departmentId) {
            this.departmentId = departmentId;
            return this;
        }
        
        public UserBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public UserBuilder loginFailCount(int loginFailCount) {
            this.loginFailCount = loginFailCount;
            return this;
        }
        
        public UserBuilder authenticationMethod(AuthenticationMethod authenticationMethod) {
            this.authenticationMethod = authenticationMethod;
            return this;
        }
        
        public UserBuilder accountExpireDate(LocalDateTime accountExpireDate) {
            this.accountExpireDate = accountExpireDate;
            return this;
        }
        
        public UserBuilder credentialsExpireDate(LocalDateTime credentialsExpireDate) {
            this.credentialsExpireDate = credentialsExpireDate;
            return this;
        }
        
        public UserBuilder lastLoginDate(LocalDateTime lastLoginDate) {
            this.lastLoginDate = lastLoginDate;
            return this;
        }
        
        public UserBuilder mfaEnabled(boolean mfaEnabled) {
            this.mfaEnabled = mfaEnabled;
            return this;
        }
        
        public UserBuilder mfaSecret(String mfaSecret) {
            this.mfaSecret = mfaSecret;
            return this;
        }
        
        public UserBuilder accountLocked(boolean accountLocked) {
            this.accountLocked = accountLocked;
            return this;
        }
        
        public UserBuilder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }
        
        public UserBuilder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }
        
        public User build() {
            User user = new User();
            user.id = this.id != null ? this.id : UUID.randomUUID();
            user.email = this.email;
            user.passwordHash = this.passwordHash;
            user.username = this.username;
            user.firstName = this.firstName;
            user.lastName = this.lastName;
            user.department = this.department;
            user.position = this.position;
            user.phone = this.phone;
            user.roleId = this.roleId;
            user.departmentId = this.departmentId;
            user.enabled = this.enabled;
            user.loginFailCount = this.loginFailCount;
            user.authenticationMethod = this.authenticationMethod;
            user.accountExpireDate = this.accountExpireDate;
            user.accountExpiresAt = this.accountExpireDate;
            user.credentialsExpireDate = this.credentialsExpireDate;
            user.passwordExpiresAt = this.credentialsExpireDate;
            user.lastLoginDate = this.lastLoginDate;
            user.lastLoginAt = this.lastLoginDate;
            user.mfaEnabled = this.mfaEnabled;
            user.mfaSecret = this.mfaSecret;
            user.accountLocked = this.accountLocked;
            user.createdAt = LocalDateTime.now();
            user.updatedAt = LocalDateTime.now();
            return user;
        }
    }
}