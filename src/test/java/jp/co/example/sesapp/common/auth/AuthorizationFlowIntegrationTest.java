package jp.co.example.sesapp.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.example.sesapp.common.auth.domain.Permission;
import jp.co.example.sesapp.common.auth.domain.ResourceType;
import jp.co.example.sesapp.common.auth.domain.ActionType;
import jp.co.example.sesapp.common.auth.domain.Role;
import jp.co.example.sesapp.common.auth.domain.User;
import jp.co.example.sesapp.common.auth.repository.PermissionRepository;
import jp.co.example.sesapp.common.auth.repository.RoleRepository;
import jp.co.example.sesapp.common.auth.repository.UserRepository;
import jp.co.example.sesapp.common.auth.service.AuthorizationService;
import jp.co.example.sesapp.common.auth.service.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PermissionRepository permissionRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AuthorizationService authorizationService;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role userRole;
    private List<Permission> adminPermissions;
    private List<Permission> userPermissions;
    private final String adminToken = "admin-jwt-token";
    private final String userToken = "user-jwt-token";

    @BeforeEach
    void setUp() {
        // 管理者ロールの設定
        adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("ADMIN");
        adminRole.setDescription("管理者ロール");

        // 一般ユーザーロールの設定
        userRole = new Role();
        userRole.setId(UUID.randomUUID());
        userRole.setName("USER");
        userRole.setDescription("一般ユーザーロール");

        // 管理者ユーザーの設定
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .name("Admin User")
                .passwordHash("hashed_admin_password")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .roleId(adminRole.getId())
                .build();

        // 一般ユーザーの設定
        regularUser = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("Regular User")
                .passwordHash("hashed_user_password")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .roleId(userRole.getId())
                .build();

        // 管理者権限の設定
        adminPermissions = Arrays.asList(
                createPermission(ResourceType.USER, "CREATE"),
                createPermission(ResourceType.USER, "READ"),
                createPermission(ResourceType.USER, "UPDATE"),
                createPermission(ResourceType.USER, "DELETE"),
                createPermission(ResourceType.ROLE, "CREATE"),
                createPermission(ResourceType.ROLE, "READ"),
                createPermission(ResourceType.ROLE, "UPDATE"),
                createPermission(ResourceType.ROLE, "DELETE")
        );

        // 一般ユーザー権限の設定
        userPermissions = Arrays.asList(
                createPermission(ResourceType.USER, "READ"),
                createPermission(ResourceType.USER, "UPDATE_SELF")
        );

        // リポジトリのモック設定
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        
        when(roleRepository.findById(adminRole.getId())).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(userRole.getId())).thenReturn(Optional.of(userRole));
        
        when(permissionRepository.findByRoleId(adminRole.getId())).thenReturn(adminPermissions);
        when(permissionRepository.findByRoleId(userRole.getId())).thenReturn(userPermissions);
        
        // JWT検証のモック設定
        when(jwtProvider.validateToken(adminToken)).thenReturn(true);
        when(jwtProvider.validateToken(userToken)).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(adminToken)).thenReturn(adminUser.getId());
        when(jwtProvider.getUserIdFromToken(userToken)).thenReturn(regularUser.getId());
        when(jwtProvider.getRoleFromToken(adminToken)).thenReturn("ADMIN");
        when(jwtProvider.getRoleFromToken(userToken)).thenReturn("USER");
        
        // 権限チェックのモック設定
        when(authorizationService.hasPermission(eq(adminUser.getId()), any(ResourceType.class), any(ActionType.class))).thenReturn(true);
        when(authorizationService.hasPermission(eq(regularUser.getId()), eq(ResourceType.USER), eq(ActionType.READ))).thenReturn(true);
        when(authorizationService.hasPermission(eq(regularUser.getId()), eq(ResourceType.USER), eq(ActionType.UPDATE_SELF))).thenReturn(true);
        when(authorizationService.hasPermission(eq(regularUser.getId()), eq(ResourceType.USER), eq(ActionType.CREATE))).thenReturn(false);
        when(authorizationService.hasPermission(eq(regularUser.getId()), eq(ResourceType.USER), eq(ActionType.DELETE))).thenReturn(false);
        when(authorizationService.hasPermission(eq(regularUser.getId()), eq(ResourceType.ROLE), any(ActionType.class))).thenReturn(false);
    }

    private Permission createPermission(ResourceType resourceType, String action) {
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setResourceType(resourceType);
        permission.setAction(action);
        return permission;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void accessAdminEndpoint_AsAdmin_ShouldAllow() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void accessAdminEndpoint_AsUser_ShouldDeny() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void accessProtectedEndpoint_AsAnonymous_ShouldDeny() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithAdminJwt_ShouldAllow() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void accessProtectedEndpoint_WithUserJwt_ShouldAllow() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void accessAdminEndpoint_WithUserJwt_ShouldDeny() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_AsAdmin_ShouldAllow() throws Exception {
        // ユーザー作成リクエストの準備
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("email", "newuser@example.com");
        createRequest.put("name", "New User");
        createRequest.put("password", "Password123!");
        createRequest.put("roleId", userRole.getId().toString());

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_AsUser_ShouldDeny() throws Exception {
        // ユーザー作成リクエストの準備
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("email", "newuser@example.com");
        createRequest.put("name", "New User");
        createRequest.put("password", "Password123!");
        createRequest.put("roleId", userRole.getId().toString());

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateOwnProfile_AsUser_ShouldAllow() throws Exception {
        // プロフィール更新リクエストの準備
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("name", "Updated Name");
        updateRequest.put("department", "Updated Department");

        // 自分自身のプロフィールを更新する場合
        when(authorizationService.isResourceOwner(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateOtherUserProfile_AsUser_ShouldDeny() throws Exception {
        // プロフィール更新リクエストの準備
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", adminUser.getId().toString());
        updateRequest.put("name", "Hacked Name");

        // 他人のプロフィールを更新する場合
        when(authorizationService.isResourceOwner(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/users/" + adminUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAnyUserProfile_AsAdmin_ShouldAllow() throws Exception {
        // プロフィール更新リクエストの準備
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("name", "Admin Updated Name");

        mockMvc.perform(post("/api/users/" + regularUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void accessRoleManagement_AsAdmin_ShouldAllow() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void accessRoleManagement_AsUser_ShouldDeny() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_AsAdmin_ShouldAllow() throws Exception {
        // ロール作成リクエストの準備
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("name", "NEW_ROLE");
        createRequest.put("description", "新しいロール");

        mockMvc.perform(post("/api/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createRole_AsUser_ShouldDeny() throws Exception {
        // ロール作成リクエストの準備
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("name", "HACKED_ROLE");
        createRequest.put("description", "ハックされたロール");

        mockMvc.perform(post("/api/admin/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessPublicEndpoint_WithoutAuthentication_ShouldAllow() throws Exception {
        mockMvc.perform(get("/api/public/info")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void accessActuatorHealth_WithoutAuthentication_ShouldAllow() throws Exception {
        mockMvc.perform(get("/actuator/health")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void accessActuatorMetrics_AsAnonymous_ShouldDeny() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void accessActuatorMetrics_AsAdmin_ShouldAllow() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}