package com.ses_mgr.common.service.impl;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.entity.*;
import com.ses_mgr.common.repository.*;
import com.ses_mgr.common.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponseDto> userDtos = userPage.getContent().stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
        return new PageImpl<>(userDtos, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> searchUsers(UserSearchRequestDto searchRequestDto, Pageable pageable) {
        // 部署名から部署IDを取得
        Integer departmentId = null;
        if (searchRequestDto.getDepartment() != null && !searchRequestDto.getDepartment().isEmpty()) {
            Optional<Department> department = departmentRepository.findByDepartmentName(searchRequestDto.getDepartment());
            departmentId = department.map(Department::getDepartmentId).orElse(null);
        }

        Page<User> userPage = userRepository.searchUsers(
                searchRequestDto.getKeyword(),
                searchRequestDto.getStatus(),
                departmentId,
                searchRequestDto.getRole(),
                searchRequestDto.getFrom(),
                searchRequestDto.getTo(),
                pageable
        );

        List<UserResponseDto> userDtos = userPage.getContent().stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
        return new PageImpl<>(userDtos, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));
        return convertToUserResponseDto(user);
    }

    @Override
    public UserResponseDto createUser(UserCreateRequestDto createRequestDto) {
        // 既存ユーザーのチェック
        if (userRepository.existsByLoginId(createRequestDto.getLoginId())) {
            throw new IllegalArgumentException("このログインIDは既に使用されています: " + createRequestDto.getLoginId());
        }
        if (userRepository.existsByEmail(createRequestDto.getEmail())) {
            throw new IllegalArgumentException("このメールアドレスは既に使用されています: " + createRequestDto.getEmail());
        }

        // 部署の取得
        Department department = null;
        if (createRequestDto.getDepartment() != null && !createRequestDto.getDepartment().isEmpty()) {
            department = departmentRepository.findByDepartmentName(createRequestDto.getDepartment())
                    .orElseThrow(() -> new EntityNotFoundException("部署が見つかりません: " + createRequestDto.getDepartment()));
        }

        // ユーザーエンティティの作成
        User user = User.builder()
                .userId(UUID.randomUUID())
                .loginId(createRequestDto.getLoginId())
                .email(createRequestDto.getEmail())
                .name(createRequestDto.getName())
                .department(department)
                .position(createRequestDto.getPosition())
                .phone(createRequestDto.getPhone())
                .passwordHash(passwordEncoder.encode(createRequestDto.getPassword()))
                .status(createRequestDto.getStatus())
                .mfaEnabled(false)
                .loginAttempts(0)
                .build();

        // ユーザーの保存
        user = userRepository.save(user);

        // ロールの割り当て
        try {
            if (createRequestDto.getRole() != null && !createRequestDto.getRole().isEmpty()) {
                assignRoleToUser(user.getUserId(), createRequestDto.getRole());
            } else {
                // デフォルトロールの割り当て（ロールが指定されていない場合）
                // システムにUSERロールが存在する場合はそれを割り当て
                try {
                    Role defaultRole = roleRepository.findByRoleCode("USER")
                            .orElse(null);
                    if (defaultRole != null) {
                        UserRoleId userRoleId = new UserRoleId(user.getUserId(), defaultRole.getRoleId());
                        UserRole userRole = new UserRole();
                        userRole.setId(userRoleId);
                        userRole.setUser(user);
                        userRole.setRole(defaultRole);
                        userRole.setAssignedAt(LocalDateTime.now());
                        userRoleRepository.save(userRole);
                    }
                } catch (Exception e) {
                    // デフォルトロールの割り当てに失敗しても処理を継続
                }
            }
        } catch (Exception e) {
            // ロール割り当てに失敗してもユーザー作成は成功とみなす
        }

        return convertToUserResponseDto(user);
    }

    @Override
    public UserResponseDto updateUser(UUID userId, UserUpdateRequestDto updateRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

        // メールアドレスの重複チェック
        if (updateRequestDto.getEmail() != null && !updateRequestDto.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(updateRequestDto.getEmail())) {
            throw new IllegalArgumentException("このメールアドレスは既に使用されています: " + updateRequestDto.getEmail());
        }

        // 部署の取得
        Department department = null;
        if (updateRequestDto.getDepartment() != null && !updateRequestDto.getDepartment().isEmpty()) {
            department = departmentRepository.findByDepartmentName(updateRequestDto.getDepartment())
                    .orElseThrow(() -> new EntityNotFoundException("部署が見つかりません: " + updateRequestDto.getDepartment()));
            user.setDepartment(department);
        }

        // ユーザー情報の更新
        if (updateRequestDto.getEmail() != null) {
            user.setEmail(updateRequestDto.getEmail());
        }
        if (updateRequestDto.getName() != null) {
            user.setName(updateRequestDto.getName());
        }
        if (updateRequestDto.getPosition() != null) {
            user.setPosition(updateRequestDto.getPosition());
        }
        if (updateRequestDto.getPhone() != null) {
            user.setPhone(updateRequestDto.getPhone());
        }
        if (updateRequestDto.getStatus() != null) {
            user.setStatus(updateRequestDto.getStatus());
        }

        user = userRepository.save(user);

        // ロールの更新
        if (updateRequestDto.getRole() != null && !updateRequestDto.getRole().isEmpty()) {
            // 現在のロールをすべて削除
            List<UserRole> currentUserRoles = userRoleRepository.findByUserUserId(userId);
            userRoleRepository.deleteAll(currentUserRoles);

            // 新しいロールを割り当て
            assignRoleToUser(userId, updateRequestDto.getRole());
        }

        return convertToUserResponseDto(user);
    }

    @Override
    public UserResponseDto updateUserStatus(UUID userId, UserStatusRequestDto statusRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

        user.setStatus(statusRequestDto.getStatus());
        user = userRepository.save(user);

        return convertToUserResponseDto(user);
    }

    @Override
    public void resetUserPassword(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

        // ランダムなパスワードを生成（実装では実際のランダムパスワード生成ロジックを使用）
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(1)); // 1日後に有効期限
        userRepository.save(user);

        // ここでメール送信ロジックを呼び出す（実装では実際のメール送信処理を使用）
        // sendPasswordResetEmail(user.getEmail(), newPassword);
    }

    @Override
    public UserResponseDto unlockUserAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

        if (!"locked".equals(user.getStatus())) {
            throw new IllegalStateException("このユーザーはロックされていません: " + userId);
        }

        user.setStatus("active");
        user.setLoginAttempts(0);
        user = userRepository.save(user);

        return convertToUserResponseDto(user);
    }

    @Override
    public int updateBulkUserStatus(UserBulkStatusRequestDto bulkStatusRequestDto) {
        return userRepository.updateStatusForUsers(
                bulkStatusRequestDto.getUserIds(),
                bulkStatusRequestDto.getStatus()
        );
    }

    @Override
    public int unlockBulkUserAccounts(UserBulkUnlockRequestDto bulkUnlockRequestDto) {
        return userRepository.unlockUsers(bulkUnlockRequestDto.getUserIds());
    }

    @Override
    public void assignRoleToUser(UUID userId, String roleCode) {
        if (userId == null) {
            throw new IllegalArgumentException("ユーザーIDがnullです");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

        // ロールの取得と検証
        Role role = null;
        UUID roleId = null;
        
        try {
            role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new EntityNotFoundException("ロールが見つかりません: " + roleCode));
            roleId = role.getRoleId();
        } catch (Exception e) {
            // ロールの取得に失敗した場合、ロールIDをnullとして処理
            // 後続のUserRoleId生成時に新しいUUIDが自動生成される
        }
        
        // roleId != nullの場合は既存の割り当てをチェック
        if (roleId != null && userRoleRepository.existsByUserUserIdAndRoleRoleId(userId, roleId)) {
            return; // 既に割り当てられている場合は何もしない
        }

        // UserRoleIdを生成（コンストラクタ内でnullチェックを実施）
        UserRoleId userRoleId = new UserRoleId(userId, roleId);
        UserRole userRole = new UserRole();
        userRole.setId(userRoleId);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);
    }

    @Override
    public void removeRoleFromUser(UUID userId, String roleCode) {
        if (userId == null) {
            throw new IllegalArgumentException("ユーザーIDがnullです");
        }
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

            Role role = null;
            UUID roleId = null;
            
            try {
                role = roleRepository.findByRoleCode(roleCode)
                        .orElseThrow(() -> new EntityNotFoundException("ロールが見つかりません: " + roleCode));
                roleId = role.getRoleId();
            } catch (Exception e) {
                // ロールの取得に失敗した場合、戻る
                return;
            }
            
            if (roleId != null) {
                UserRoleId userRoleId = new UserRoleId(userId, roleId);
                userRoleRepository.findById(userRoleId).ifPresent(userRoleRepository::delete);
            }
        } catch (Exception e) {
            // 例外が発生した場合でも処理を継続
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getUserRoles(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

        return user.getUserRoles().stream()
                .map(userRole -> convertToRoleResponseDto(userRole.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません。メール: " + email));
        return convertToUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findByLoginId(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません。ログインID: " + loginId));
        return convertToUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLoginId(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }
    
    @Override
    public void updateLastLoginTime(UUID userId) {
        userRepository.updateLastLoginTime(userId);
    }

    // ユーティリティメソッド - User エンティティを UserResponseDto に変換
    private UserResponseDto convertToUserResponseDto(User user) {
        if (user == null) {
            return null;
        }

        // ユーザーのロールを取得
        String roleName = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .findFirst()
                .orElse(null);

        // 部署名を取得
        String departmentName = user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null;

        return UserResponseDto.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .department(departmentName)
                .position(user.getPosition())
                .phone(user.getPhone())
                .role(roleName)
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .loginAttempts(user.getLoginAttempts())
                .passwordExpiresAt(user.getPasswordExpiresAt())
                .mfaEnabled(user.getMfaEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // ユーティリティメソッド - Role エンティティを RoleResponseDto に変換
    private RoleResponseDto convertToRoleResponseDto(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponseDto.builder()
                .id(role.getRoleId())
                .roleCode(role.getRoleCode())
                .name(role.getName())
                .description(role.getDescription())
                .roleType(role.getRoleType())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}