package jp.co.example.sesapp.common.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jp.co.example.sesapp.common.auth.domain.ActionType;
import jp.co.example.sesapp.common.auth.domain.ResourceType;
import jp.co.example.sesapp.common.auth.domain.dto.PasswordChangeRequest;
import jp.co.example.sesapp.common.auth.domain.dto.UserCreateDto;
import jp.co.example.sesapp.common.auth.domain.dto.UserDto;
import jp.co.example.sesapp.common.auth.domain.dto.UserUpdateDto;
import jp.co.example.sesapp.common.auth.service.AuthorizationService;
import jp.co.example.sesapp.common.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ユーザー管理に関するエンドポイントを提供するコントローラー
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "ユーザー管理API", description = "ユーザー情報の管理に関する操作を提供するAPI")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuthorizationService authorizationService;

    public UserController(UserService userService, AuthorizationService authorizationService) {
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    /**
     * すべてのユーザーを取得するエンドポイント
     *
     * @return ユーザーリスト
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "全ユーザー取得", description = "システム内のすべてのユーザーを取得します")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        // ADMIN権限があることを確認（@PreAuthorizeアノテーションでも確認している）
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 特定のユーザーを取得するエンドポイント
     *
     * @param userId ユーザーID
     * @return ユーザー情報
     */
    @GetMapping("/{userId}")
    @Operation(summary = "ユーザー取得", description = "指定されたIDのユーザー情報を取得します")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        // 自分自身の情報を取得する場合は許可
        // 他のユーザーの情報を取得する場合はUSER_READ権限が必要
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 新規ユーザーを作成するエンドポイント
     *
     * @param userCreateDto ユーザー作成DTO
     * @return 作成されたユーザー情報
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ユーザー作成", description = "新しいユーザーを作成します")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.CREATE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDto createdUser = userService.createUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * ユーザー情報を更新するエンドポイント
     *
     * @param userId ユーザーID
     * @param userUpdateDto ユーザー更新DTO
     * @return 更新されたユーザー情報
     */
    @PutMapping("/{userId}")
    @Operation(summary = "ユーザー更新", description = "指定されたユーザーの情報を更新します")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID userId, 
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        // 自分自身の情報を更新する場合は許可
        // 他のユーザーの情報を更新する場合はUSER_UPDATE権限が必要
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.UPDATE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDto updatedUser = userService.updateUser(userId, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * ユーザーを削除するエンドポイント
     *
     * @param userId ユーザーID
     * @return 空のレスポンス
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ユーザー削除", description = "指定されたユーザーを削除します")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.DELETE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ユーザーのパスワードを変更するエンドポイント
     *
     * @param userId ユーザーID
     * @param request パスワード変更リクエスト
     * @return 更新されたユーザー情報
     */
    @PostMapping("/{userId}/change-password")
    @Operation(summary = "パスワード変更", description = "指定されたユーザーのパスワードを変更します")
    public ResponseEntity<UserDto> changePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        // 自分自身のパスワードを変更する場合は許可
        // 他のユーザーのパスワードを変更する場合はADMIN権限が必要
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.UPDATE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDto user = userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(user);
    }

    /**
     * ユーザーを有効化するエンドポイント
     *
     * @param userId ユーザーID
     * @return 更新されたユーザー情報
     */
    @PostMapping("/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ユーザー有効化", description = "指定されたユーザーを有効化します")
    public ResponseEntity<UserDto> enableUser(@PathVariable UUID userId) {
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.UPDATE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDto user = userService.enableUser(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * ユーザーを無効化するエンドポイント
     *
     * @param userId ユーザーID
     * @return 更新されたユーザー情報
     */
    @PostMapping("/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ユーザー無効化", description = "指定されたユーザーを無効化します")
    public ResponseEntity<UserDto> disableUser(@PathVariable UUID userId) {
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.UPDATE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDto user = userService.disableUser(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * ユーザーのロックを解除するエンドポイント
     *
     * @param userId ユーザーID
     * @return 更新されたユーザー情報
     */
    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ユーザーロック解除", description = "指定されたユーザーのアカウントロックを解除します")
    public ResponseEntity<UserDto> unlockUser(@PathVariable UUID userId) {
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.UPDATE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDto user = userService.unlockUser(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 部門に所属するユーザーを取得するエンドポイント
     *
     * @param departmentId 部門ID
     * @return ユーザーリスト
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "部門ユーザー取得", description = "指定された部門に所属するユーザーを取得します")
    public ResponseEntity<List<UserDto>> getUsersByDepartmentId(@PathVariable UUID departmentId) {
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<UserDto> users = userService.getUsersByDepartmentId(departmentId);
        return ResponseEntity.ok(users);
    }

    /**
     * 特定の役割を持つユーザーを取得するエンドポイント
     *
     * @param roleId 役割ID
     * @return ユーザーリスト
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ロール別ユーザー取得", description = "指定された役割を持つユーザーを取得します")
    public ResponseEntity<List<UserDto>> getUsersByRoleId(@PathVariable UUID roleId) {
        if (!authorizationService.isCurrentUserAuthorized(ResourceType.USER, ActionType.READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<UserDto> users = userService.getUsersByRoleId(roleId);
        return ResponseEntity.ok(users);
    }
}