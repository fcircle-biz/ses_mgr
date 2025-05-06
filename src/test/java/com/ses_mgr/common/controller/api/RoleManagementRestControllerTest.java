package com.ses_mgr.common.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ses_mgr.common.dto.*;
import com.ses_mgr.common.exception.ApiExceptionHandler;
import com.ses_mgr.common.service.RoleManagementService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RoleManagementRestControllerTest {

    @Mock
    private RoleManagementService roleManagementService;

    @InjectMocks
    private RoleManagementRestController roleManagementRestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private UUID testRoleId;
    private RoleResponseDto testRoleDto;
    private PermissionResponseDto testPermissionDto;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders
                .standaloneSetup(roleManagementRestController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        
        testRoleId = UUID.randomUUID();
        testRoleDto = createTestRoleResponseDto();
        testPermissionDto = createTestPermissionResponseDto();
    }

    @Test
    public void getRoles_ShouldReturnRolesList() throws Exception {
        // Given
        List<RoleResponseDto> rolesList = Arrays.asList(testRoleDto, createAnotherTestRoleResponseDto());
        when(roleManagementService.getRoles(any(RoleSearchRequestDto.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(rolesList));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles", hasSize(2)))
                .andExpect(jsonPath("$.data.roles[0].id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.data.roles[0].name").value("テストロール"))
                .andExpect(jsonPath("$.data.pagination.total").value(2));
    }

    @Test
    public void createRole_WithValidData_ShouldCreateRole() throws Exception {
        // Given
        RoleCreateRequestDto createRequestDto = new RoleCreateRequestDto();
        createRequestDto.setName("新しいロール");
        createRequestDto.setDescription("新しいロールの説明");
        createRequestDto.setPermissions(Collections.singletonList("test.permission"));

        RoleResponseDto createdRoleDto = RoleResponseDto.builder()
                .id(UUID.randomUUID())
                .name("新しいロール")
                .description("新しいロールの説明")
                .roleCode("new_role")
                .permissionCount(1)
                .build();

        when(roleManagementService.createRole(any(RoleCreateRequestDto.class))).thenReturn(createdRoleDto);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("新しいロール"))
                .andExpect(jsonPath("$.data.description").value("新しいロールの説明"))
                .andExpect(jsonPath("$.data.roleCode").value("new_role"));
    }

    @Test
    public void getRoleById_WhenRoleExists_ShouldReturnRole() throws Exception {
        // Given
        when(roleManagementService.getRoleById(testRoleId)).thenReturn(testRoleDto);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/roles/{id}", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.data.roleCode").value("TEST_ROLE"))
                .andExpect(jsonPath("$.data.name").value("テストロール"));
    }

    @Test
    public void getRoleById_WhenRoleDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(roleManagementService.getRoleById(any(UUID.class)))
                .thenThrow(new EntityNotFoundException("指定されたロールが見つかりません"));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/roles/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    public void updateRole_WithValidData_ShouldUpdateRole() throws Exception {
        // Given
        RoleUpdateRequestDto updateRequestDto = new RoleUpdateRequestDto();
        updateRequestDto.setName("更新されたロール名");
        updateRequestDto.setDescription("更新された説明");

        RoleResponseDto updatedRoleDto = RoleResponseDto.builder()
                .id(testRoleId)
                .name("更新されたロール名")
                .description("更新された説明")
                .roleCode("TEST_ROLE")
                .build();

        when(roleManagementService.updateRole(eq(testRoleId), any(RoleUpdateRequestDto.class)))
                .thenReturn(updatedRoleDto);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/roles/{id}", testRoleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.data.name").value("更新されたロール名"))
                .andExpect(jsonPath("$.data.description").value("更新された説明"));
    }

    @Test
    public void deleteRole_ShouldReturnSuccess() throws Exception {
        // Given
        Map<String, Object> deletionResult = new HashMap<>();
        deletionResult.put("message", "ロールが正常に削除されました");
        deletionResult.put("id", testRoleId);

        when(roleManagementService.deleteRole(testRoleId)).thenReturn(deletionResult);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/roles/{id}", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("ロールが正常に削除されました"))
                .andExpect(jsonPath("$.data.id").value(testRoleId.toString()));
    }

    @Test
    public void getRolePermissions_ShouldReturnPermissionsList() throws Exception {
        // Given
        List<PermissionResponseDto> permissionsList = Arrays.asList(testPermissionDto);
        when(roleManagementService.getRolePermissions(testRoleId)).thenReturn(permissionsList);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/roles/{id}/permissions", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].permissionCode").value("test.permission"))
                .andExpect(jsonPath("$.data[0].name").value("テスト権限"));
    }

    @Test
    public void updateRolePermissions_ShouldUpdatePermissions() throws Exception {
        // Given
        RolePermissionUpdateRequestDto updateRequestDto = new RolePermissionUpdateRequestDto();
        updateRequestDto.setPermissions(Arrays.asList("test.permission", "new.permission"));

        Map<String, Object> updateResult = new HashMap<>();
        updateResult.put("id", testRoleId);
        updateResult.put("name", "テストロール");
        updateResult.put("permissionCount", 2);
        updateResult.put("added", Collections.singletonList("new.permission"));
        updateResult.put("removed", Collections.emptyList());

        when(roleManagementService.updateRolePermissions(eq(testRoleId), any(RolePermissionUpdateRequestDto.class)))
                .thenReturn(updateResult);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/roles/{id}/permissions", testRoleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.data.permissionCount").value(2))
                .andExpect(jsonPath("$.data.added[0]").value("new.permission"));
    }

    @Test
    public void getAllPermissions_ShouldReturnPermissionsList() throws Exception {
        // Given
        List<PermissionResponseDto> permissionsList = Arrays.asList(testPermissionDto);
        when(roleManagementService.getAllPermissions(null, null)).thenReturn(permissionsList);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].permissionCode").value("test.permission"))
                .andExpect(jsonPath("$.data[0].name").value("テスト権限"));
    }

    @Test
    public void getAllPermissions_WithFilters_ShouldReturnFilteredPermissions() throws Exception {
        // Given
        List<PermissionResponseDto> permissionsList = Arrays.asList(testPermissionDto);
        when(roleManagementService.getAllPermissions("test", null)).thenReturn(permissionsList);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/permissions")
                        .param("resource", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].permissionCode").value("test.permission"));
    }

    // ユーティリティメソッド
    private RoleResponseDto createTestRoleResponseDto() {
        return RoleResponseDto.builder()
                .id(testRoleId)
                .roleCode("TEST_ROLE")
                .name("テストロール")
                .description("テスト用のロール")
                .isSystem(false)
                .permissionCount(1)
                .userCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private RoleResponseDto createAnotherTestRoleResponseDto() {
        return RoleResponseDto.builder()
                .id(UUID.randomUUID())
                .roleCode("ADMIN_ROLE")
                .name("管理者ロール")
                .description("管理者用のロール")
                .isSystem(true)
                .permissionCount(10)
                .userCount(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private PermissionResponseDto createTestPermissionResponseDto() {
        return PermissionResponseDto.builder()
                .id(UUID.randomUUID())
                .permissionCode("test.permission")
                .name("テスト権限")
                .description("テスト用の権限")
                .resourceType("test")
                .resourceName("resource")
                .action("read")
                .accessLevel("write")
                .isAssigned(true)
                .build();
    }
}