package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class RoleRestController {

    private final RoleService roleService;
    
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
        Page<RoleResponseDto> rolesPage = roleService.getRoles(searchRequestDto, pageable);
        
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
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> createRole(
            @Valid @RequestBody RoleCreateRequestDto createRequestDto) {
        
        RoleResponseDto createdRole = roleService.createRole(createRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(createdRole));
    }
    
    /**
     * 特定ロールの詳細取得
     */
    @GetMapping("/roles/{id}")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleById(
            @PathVariable UUID id) {
        
        RoleResponseDto role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponseDto.success(role));
    }
    
    /**
     * ロール情報の更新
     */
    @PutMapping("/roles/{id}")
    public ResponseEntity<ApiResponseDto<RoleResponseDto>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateRequestDto updateRequestDto) {
        
        RoleResponseDto updatedRole = roleService.updateRole(id, updateRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(updatedRole));
    }
    
    /**
     * ロールの削除
     */
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> deleteRole(
            @PathVariable UUID id) {
        
        Map<String, Object> deletionResult = roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponseDto.success(deletionResult));
    }
    
    /**
     * ロールの権限一覧取得
     */
    @GetMapping("/roles/{id}/permissions")
    public ResponseEntity<ApiResponseDto<List<PermissionResponseDto>>> getRolePermissions(
            @PathVariable UUID id) {
        
        List<PermissionResponseDto> permissions = roleService.getRolePermissions(id);
        return ResponseEntity.ok(ApiResponseDto.success(permissions));
    }
    
    /**
     * ロールの権限更新
     */
    @PutMapping("/roles/{id}/permissions")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateRolePermissions(
            @PathVariable UUID id,
            @Valid @RequestBody RolePermissionUpdateRequestDto updateRequestDto) {
        
        Map<String, Object> updateResult = roleService.updateRolePermissions(id, updateRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(updateResult));
    }
    
    /**
     * 全権限一覧取得
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponseDto<List<PermissionResponseDto>>> getAllPermissions(
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String action) {
        
        List<PermissionResponseDto> permissions = roleService.getAllPermissions(resource, action);
        return ResponseEntity.ok(ApiResponseDto.success(permissions));
    }
}