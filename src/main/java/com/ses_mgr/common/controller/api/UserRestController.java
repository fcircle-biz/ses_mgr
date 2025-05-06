package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserRestController {

    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    // ユーザー一覧の取得
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponseDto.success(users));
    }

    // ユーザー検索
    @PostMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> searchUsers(
            @RequestBody UserSearchRequestDto searchRequestDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> users = userService.searchUsers(searchRequestDto, pageable);
        
        return ResponseEntity.ok(ApiResponseDto.success(users));
    }

    // 特定ユーザーの詳細取得
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUserById(@PathVariable UUID userId) {
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponseDto.success(user));
    }

    // 新規ユーザーの作成
    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(@Valid @RequestBody UserCreateRequestDto createRequestDto) {
        UserResponseDto createdUser = userService.createUser(createRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(createdUser));
    }

    // ユーザー情報の更新
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequestDto updateRequestDto) {
        
        UserResponseDto updatedUser = userService.updateUser(userId, updateRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(updatedUser));
    }

    // ユーザーステータスの変更
    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UserStatusRequestDto statusRequestDto) {
        
        UserResponseDto updatedUser = userService.updateUserStatus(userId, statusRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(updatedUser));
    }

    // ユーザーパスワードのリセット
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> resetUserPassword(@PathVariable UUID userId) {
        userService.resetUserPassword(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "パスワードが正常にリセットされました");
        response.put("id", userId);
        
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    // ユーザーアカウントのロック解除
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> unlockUserAccount(@PathVariable UUID userId) {
        UserResponseDto unlockedUser = userService.unlockUserAccount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "アカウントロックが解除されました");
        response.put("id", userId);
        response.put("status", unlockedUser.getStatus());
        
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    // 複数ユーザーステータスの一括変更
    @PutMapping("/bulk-status")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateBulkUserStatus(
            @Valid @RequestBody UserBulkStatusRequestDto bulkStatusRequestDto) {
        
        int updatedCount = userService.updateBulkUserStatus(bulkStatusRequestDto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", updatedCount + " 件のユーザーステータスが更新されました");
        response.put("count", updatedCount);
        
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    // 複数ユーザーの一括ロック解除
    @PostMapping("/bulk-unlock")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> unlockBulkUserAccounts(
            @Valid @RequestBody UserBulkUnlockRequestDto bulkUnlockRequestDto) {
        
        int unlockedCount = userService.unlockBulkUserAccounts(bulkUnlockRequestDto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", unlockedCount + " 件のユーザーアカウントのロックが解除されました");
        response.put("count", unlockedCount);
        
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    // ユーザーのロール一覧取得
    @GetMapping("/{userId}/roles")
    public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> getUserRoles(@PathVariable UUID userId) {
        List<RoleResponseDto> roles = userService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponseDto.success(roles));
    }

    // ユーザーへのロール割り当て
    @PostMapping("/{userId}/roles/{roleCode}")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> assignRoleToUser(
            @PathVariable UUID userId,
            @PathVariable String roleCode) {
        
        userService.assignRoleToUser(userId, roleCode);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "ロールが正常に割り当てられました");
        response.put("userId", userId);
        response.put("roleCode", roleCode);
        
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    // ユーザーからのロール削除
    @DeleteMapping("/{userId}/roles/{roleCode}")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> removeRoleFromUser(
            @PathVariable UUID userId,
            @PathVariable String roleCode) {
        
        userService.removeRoleFromUser(userId, roleCode);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "ロールが正常に削除されました");
        response.put("userId", userId);
        response.put("roleCode", roleCode);
        
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }
}