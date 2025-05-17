package jp.co.example.sesapp.common.auth.service.impl;

import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.domain.dto.UserCreateDto;
import jp.co.example.sesapp.common.auth.domain.dto.UserDto;
import jp.co.example.sesapp.common.auth.domain.dto.UserUpdateDto;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.auth.service.UserService;
import jp.co.example.sesapp.common.exception.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import jp.co.example.sesapp.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ユーザー管理サービスの実装クラス
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.password.expiration-days}")
    private int passwordExpirationDays;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return convertToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByDepartmentId(UUID departmentId) {
        return userRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRoleId(UUID roleId) {
        return userRepository.findByRoleId(roleId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto userCreateDto) {
        // ユーザー名とメールアドレスの重複チェック
        if (userRepository.findByUsername(userCreateDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.findByEmail(userCreateDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // ユーザーエンティティの作成
        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userCreateDto.getPassword()));
        user.setFirstName(userCreateDto.getFirstName());
        user.setLastName(userCreateDto.getLastName());
        user.setDepartmentId(userCreateDto.getDepartmentId());

        // アカウント設定
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setLoginFailCount(0);
        user.setMfaEnabled(false);

        // パスワード有効期限の設定
        if (passwordExpirationDays > 0) {
            user.setCredentialsExpireDate(LocalDateTime.now().plusDays(passwordExpirationDays));
        }

        // ユーザーの保存
        User savedUser = userRepository.save(user);

        // 初期ロールの割り当て
        if (userCreateDto.getRoleIds() != null && !userCreateDto.getRoleIds().isEmpty()) {
            for (UUID roleId : userCreateDto.getRoleIds()) {
                roleRepository.assignRoleToUser(savedUser.getId(), roleId);
            }
        }

        return convertToDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID userId, UserUpdateDto userUpdateDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // ユーザー名とメールアドレスの重複チェック（自分自身は除く）
        if (userUpdateDto.getUsername() != null && 
                !userUpdateDto.getUsername().equals(existingUser.getUsername()) && 
                userRepository.findByUsername(userUpdateDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userUpdateDto.getEmail() != null && 
                !userUpdateDto.getEmail().equals(existingUser.getEmail()) && 
                userRepository.findByEmail(userUpdateDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // 更新対象フィールドのみ更新
        if (userUpdateDto.getUsername() != null) {
            existingUser.setUsername(userUpdateDto.getUsername());
        }
        if (userUpdateDto.getEmail() != null) {
            existingUser.setEmail(userUpdateDto.getEmail());
        }
        if (userUpdateDto.getFirstName() != null) {
            existingUser.setFirstName(userUpdateDto.getFirstName());
        }
        if (userUpdateDto.getLastName() != null) {
            existingUser.setLastName(userUpdateDto.getLastName());
        }
        if (userUpdateDto.getDepartmentId() != null) {
            existingUser.setDepartmentId(userUpdateDto.getDepartmentId());
        }

        // パスワードが指定されている場合は更新
        if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(userUpdateDto.getPassword()));
            
            // パスワード有効期限の設定
            if (passwordExpirationDays > 0) {
                existingUser.setCredentialsExpireDate(LocalDateTime.now().plusDays(passwordExpirationDays));
            }
        }

        // ユーザーの保存
        User updatedUser = userRepository.save(existingUser);

        // 役割の更新（指定されている場合）
        if (userUpdateDto.getRoleIds() != null) {
            // 既存の役割をすべて削除
            roleRepository.findByUserId(userId).forEach(role -> 
                    roleRepository.removeRoleFromUser(userId, role.getId()));

            // 新しい役割を割り当て
            userUpdateDto.getRoleIds().forEach(roleId -> 
                    roleRepository.assignRoleToUser(userId, roleId));
        }

        return convertToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserDto changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 現在のパスワードを確認
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // 新しいパスワードと現在のパスワードが同じでないことを確認
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // パスワードの更新
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        
        // パスワード有効期限の設定
        if (passwordExpirationDays > 0) {
            user.setCredentialsExpireDate(LocalDateTime.now().plusDays(passwordExpirationDays));
        }

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto enableUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(true);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto disableUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(false);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto unlockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setAccountLocked(false);
        user.setLoginFailCount(0);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    /**
     * UserエンティティをUserDtoに変換するヘルパーメソッド
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setEnabled(user.isEnabled());
        dto.setAccountLocked(user.isAccountLocked());
        dto.setMfaEnabled(user.isMfaEnabled());
        dto.setAccountExpireDate(user.getAccountExpireDate());
        dto.setCredentialsExpireDate(user.getCredentialsExpireDate());
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // ユーザーのロール情報を取得
        dto.setRoles(roleRepository.findByUserId(user.getId()).stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));
        
        return dto;
    }
}