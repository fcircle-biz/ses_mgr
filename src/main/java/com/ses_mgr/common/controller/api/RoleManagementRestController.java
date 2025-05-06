package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.service.RoleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyAuthority('system.roles.read', 'system.roles.admin', 'system.admin')")
@RequiredArgsConstructor
public class RoleManagementRestController {

    private final RoleManagementService roleManagementService;
    
    /**
     * ロール一覧の取得
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "true") Boolean includeSystem,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        
        // ページングの制限チェック
        if (limit > 100) {
            limit = 100;
        }
        
        RoleSearchRequestDto searchRequestDto = RoleSearchRequestDto.builder()
                .search(search)
                .includeSystem(includeSystem)
                .build();
        
        Pageable pageable = PageRequest.of(page, limit);
        Page<RoleResponseDto> rolesPage = roleManagementService.getRoles(searchRequestDto, pageable);
        
        // ページネーション情報を作成
        PaginationResponseDto pagination = PaginationResponseDto.builder()
                .total(rolesPage.getTotalElements())
                .page(page)
                .limit(limit)
                .pages(rolesPage.getTotalPages())
                .build();
        
        // レスポンスをマッピング
        Map<String, Object> response = new HashMap<>();
        response.put("roles", rolesPage.getContent());
        response.put("pagination", pagination);
        
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }
    
    /**
     * 新規ロールの作成
     */
    @PostMapping("/roles")
    @PreAuthorize("hasAnyAuthority('system.roles.admin', 'system.admin')")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> createRole(
            @Valid @RequestBody RoleCreateRequestDto createRequestDto) {
        
        RoleResponseDto createdRole = roleManagementService.createRole(createRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(createdRole));
    }
    
    /**
     * 特定ロールの詳細取得
     */
    @GetMapping("/roles/{id}")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleById(
            @PathVariable UUID id) {
        
        RoleResponseDto role = roleManagementService.getRoleById(id);
        return ResponseEntity.ok(ApiResponseDto.success(role));
    }
    
    /**
     * ロール情報の更新
     */
    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAnyAuthority('system.roles.admin', 'system.admin')")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateRequestDto updateRequestDto) {
        
        RoleResponseDto updatedRole = roleManagementService.updateRole(id, updateRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(updatedRole));
    }
    
    /**
     * ロールの削除
     */
    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAnyAuthority('system.roles.admin', 'system.admin')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> deleteRole(
            @PathVariable UUID id) {
        
        Map<String, Object> deletionResult = roleManagementService.deleteRole(id);
        return ResponseEntity.ok(ApiResponseDto.success(deletionResult));
    }
    
    /**
     * ロールの権限一覧取得
     */
    @GetMapping("/roles/{id}/permissions")
    public ResponseEntity<ApiResponseDto<List<PermissionResponseDto>>> getRolePermissions(
            @PathVariable UUID id) {
        
        List<PermissionResponseDto> permissions = roleManagementService.getRolePermissions(id);
        return ResponseEntity.ok(ApiResponseDto.success(permissions));
    }
    
    /**
     * ロールの権限更新
     */
    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAnyAuthority('system.roles.admin', 'system.admin')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateRolePermissions(
            @PathVariable UUID id,
            @Valid @RequestBody RolePermissionUpdateRequestDto updateRequestDto) {
        
        Map<String, Object> updateResult = roleManagementService.updateRolePermissions(id, updateRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(updateResult));
    }
    
    /**
     * 全権限一覧取得
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponseDto<List<PermissionResponseDto>>> getAllPermissions(
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String action) {
        
        List<PermissionResponseDto> permissions = roleManagementService.getAllPermissions(resource, action);
        return ResponseEntity.ok(ApiResponseDto.success(permissions));
    }
}