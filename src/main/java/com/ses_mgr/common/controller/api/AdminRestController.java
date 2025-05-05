package com.ses_mgr.common.controller.api;

import com.ses_mgr.common.service.BatchService;
import com.ses_mgr.common.service.MasterDataService;
import com.ses_mgr.common.service.RoleService;
import com.ses_mgr.common.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * システム管理機能のREST APIコントローラ
 */
@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final RoleService roleService;
    private final MasterDataService masterDataService;
    private final BatchService batchService;

    @Autowired
    public AdminRestController(UserService userService, RoleService roleService, 
                              MasterDataService masterDataService, BatchService batchService) {
        this.userService = userService;
        this.roleService = roleService;
        this.masterDataService = masterDataService;
        this.batchService = batchService;
    }

    /**
     * ユーザー一覧を取得する
     *
     * @return ユーザー一覧
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userService.searchUsers(null));
    }

    /**
     * ユーザーを検索する
     *
     * @param searchCriteria 検索条件
     * @return 検索結果
     */
    @PostMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestBody Object searchCriteria) {
        return ResponseEntity.ok(userService.searchUsers(searchCriteria));
    }

    /**
     * ユーザーを作成する
     *
     * @param userData ユーザーデータ
     * @return 作成結果
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Object userData) {
        return ResponseEntity.ok(userService.createUser(userData));
    }

    /**
     * ユーザーを更新する
     *
     * @param userId ユーザーID
     * @param userData ユーザーデータ
     * @return 更新結果
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody Object userData) {
        return ResponseEntity.ok(userService.updateUser(userId, userData));
    }

    /**
     * ユーザーのステータスを変更する
     *
     * @param userId ユーザーID
     * @param status 新しいステータス
     * @return 更新結果
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> changeUserStatus(@PathVariable String userId, @RequestBody String status) {
        return ResponseEntity.ok(userService.changeUserStatus(userId, status));
    }

    /**
     * パスワードをリセットする
     *
     * @param userId ユーザーID
     * @return リセット結果
     */
    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String userId) {
        return ResponseEntity.ok(userService.resetPassword(userId));
    }
    
    /**
     * ユーザー詳細を取得する
     *
     * @param userId ユーザーID
     * @return ユーザー詳細情報
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable String userId) {
        // サンプルデータとしてユーザーサービスから該当するユーザーを取得
        List<Map<String, Object>> users = (List<Map<String, Object>>) userService.searchUsers(null);
        for (Map<String, Object> user : users) {
            if (user.get("id").equals(userId)) {
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * ユーザーのロックを解除する
     *
     * @param userId ユーザーID
     * @return ロック解除結果
     */
    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.unlockUser(userId));
    }
    
    /**
     * 複数ユーザーのステータスを一括変更する
     *
     * @param request リクエスト（userIds: ユーザーIDリスト, status: ステータス）
     * @return 変更結果
     */
    @PutMapping("/users/bulk-status")
    public ResponseEntity<?> bulkChangeUserStatus(@RequestBody Map<String, Object> request) {
        List<String> userIds = (List<String>) request.get("userIds");
        String status = (String) request.get("status");
        return ResponseEntity.ok(userService.bulkChangeUserStatus(userIds, status));
    }
    
    /**
     * 複数ユーザーのロックを一括解除する
     *
     * @param request リクエスト（userIds: ユーザーIDリスト）
     * @return ロック解除結果
     */
    @PostMapping("/users/bulk-unlock")
    public ResponseEntity<?> bulkUnlockUsers(@RequestBody Map<String, Object> request) {
        List<String> userIds = (List<String>) request.get("userIds");
        return ResponseEntity.ok(userService.bulkUnlockUsers(userIds));
    }

    /**
     * 全ロールを取得する
     *
     * @return ロール一覧
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    /**
     * ロールの詳細を取得する
     *
     * @param roleId ロールID
     * @return ロール詳細
     */
    @GetMapping("/roles/{roleId}")
    public ResponseEntity<?> getRoleDetails(@PathVariable String roleId) {
        return ResponseEntity.ok(roleService.getRoleDetails(roleId));
    }

    /**
     * ロールを作成する
     *
     * @param roleData ロールデータ
     * @return 作成結果
     */
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody Object roleData) {
        return ResponseEntity.ok(roleService.createRole(roleData));
    }

    /**
     * ロールを更新する
     *
     * @param roleId ロールID
     * @param roleData ロールデータ
     * @return 更新結果
     */
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<?> updateRole(@PathVariable String roleId, @RequestBody Object roleData) {
        return ResponseEntity.ok(roleService.updateRole(roleId, roleData));
    }

    /**
     * ロールを削除する
     *
     * @param roleId ロールID
     * @return 削除結果
     */
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable String roleId) {
        return ResponseEntity.ok(roleService.deleteRole(roleId));
    }

    /**
     * ロールにユーザーを追加する
     *
     * @param roleId ロールID
     * @param userIds ユーザーIDのリスト
     * @return 追加結果
     */
    @PostMapping("/roles/{roleId}/users")
    public ResponseEntity<?> addUsersToRole(@PathVariable String roleId, @RequestBody Object userIds) {
        return ResponseEntity.ok(roleService.addUsersToRole(roleId, userIds));
    }

    /**
     * ロールからユーザーを削除する
     *
     * @param roleId ロールID
     * @param userId ユーザーID
     * @return 削除結果
     */
    @DeleteMapping("/roles/{roleId}/users/{userId}")
    public ResponseEntity<?> removeUserFromRole(@PathVariable String roleId, @PathVariable String userId) {
        return ResponseEntity.ok(roleService.removeUserFromRole(roleId, userId));
    }

    /**
     * 部門マスタデータを取得する
     *
     * @return 部門一覧
     */
    @GetMapping("/masters/departments")
    public ResponseEntity<?> getDepartments() {
        return ResponseEntity.ok(masterDataService.getDepartments());
    }

    /**
     * 部門を作成する
     *
     * @param departmentData 部門データ
     * @return 作成結果
     */
    @PostMapping("/masters/departments")
    public ResponseEntity<?> createDepartment(@RequestBody Object departmentData) {
        return ResponseEntity.ok(masterDataService.createDepartment(departmentData));
    }

    /**
     * 部門を更新する
     *
     * @param departmentId 部門ID
     * @param departmentData 部門データ
     * @return 更新結果
     */
    @PutMapping("/masters/departments/{departmentId}")
    public ResponseEntity<?> updateDepartment(@PathVariable Integer departmentId, @RequestBody Object departmentData) {
        return ResponseEntity.ok(masterDataService.updateDepartment(departmentId, departmentData));
    }

    /**
     * 部門を削除する
     *
     * @param departmentId 部門ID
     * @return 削除結果
     */
    @DeleteMapping("/masters/departments/{departmentId}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Integer departmentId) {
        return ResponseEntity.ok(masterDataService.deleteDepartment(departmentId));
    }

    /**
     * 企業マスタデータを取得する
     *
     * @return 企業一覧
     */
    @GetMapping("/masters/companies")
    public ResponseEntity<?> getCompanies() {
        return ResponseEntity.ok(masterDataService.getCompanies());
    }

    /**
     * スキルマスタデータを取得する
     *
     * @return スキル一覧
     */
    @GetMapping("/masters/skills")
    public ResponseEntity<?> getSkills() {
        return ResponseEntity.ok(masterDataService.getSkills());
    }

    /**
     * コード値マスタデータを取得する
     *
     * @param codeType コード種別
     * @return コード値一覧
     */
    @GetMapping("/masters/codes/{codeType}")
    public ResponseEntity<?> getCodeValues(@PathVariable String codeType) {
        return ResponseEntity.ok(masterDataService.getCodeValues(codeType));
    }

    /**
     * 休日マスタデータを取得する
     *
     * @param year 年
     * @return 休日一覧
     */
    @GetMapping("/masters/holidays")
    public ResponseEntity<?> getHolidays(@RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(masterDataService.getHolidays(year));
    }
    
    /**
     * 定期バッチ一覧を取得する
     *
     * @return 定期バッチ一覧
     */
    @GetMapping("/batch/scheduled")
    public ResponseEntity<?> getScheduledBatches() {
        return ResponseEntity.ok(batchService.getScheduledBatches());
    }
    
    /**
     * オンデマンドバッチ一覧を取得する
     *
     * @return オンデマンドバッチ一覧
     */
    @GetMapping("/batch/ondemand")
    public ResponseEntity<?> getOnDemandBatches() {
        return ResponseEntity.ok(batchService.getOnDemandBatches());
    }
    
    /**
     * バッチ実行履歴を取得する
     *
     * @param searchParams 検索条件
     * @return バッチ実行履歴
     */
    @PostMapping("/batch/history")
    public ResponseEntity<?> getBatchHistory(@RequestBody(required = false) Map<String, Object> searchParams) {
        return ResponseEntity.ok(batchService.getBatchHistory(searchParams != null ? searchParams : new HashMap<>()));
    }
    
    /**
     * バッチの詳細情報を取得する
     *
     * @param batchId バッチID
     * @return バッチ詳細情報
     */
    @GetMapping("/batch/{batchId}")
    public ResponseEntity<?> getBatchDetails(@PathVariable String batchId) {
        return ResponseEntity.ok(batchService.getBatchDetails(batchId));
    }
    
    /**
     * バッチの実行詳細情報を取得する
     *
     * @param executionId 実行ID
     * @return 実行詳細情報
     */
    @GetMapping("/batch/execution/{executionId}")
    public ResponseEntity<?> getExecutionDetails(@PathVariable String executionId) {
        return ResponseEntity.ok(batchService.getExecutionDetails(executionId));
    }
    
    /**
     * バッチを実行する
     *
     * @param batchId バッチID
     * @param parameters 実行パラメータ
     * @return 実行結果
     */
    @PostMapping("/batch/{batchId}/execute")
    public ResponseEntity<?> executeBatch(
            @PathVariable String batchId,
            @RequestBody(required = false) Map<String, Object> parameters) {
        return ResponseEntity.ok(batchService.executeBatch(batchId, parameters != null ? parameters : new HashMap<>()));
    }
    
    /**
     * バッチのスケジュールを更新する
     *
     * @param batchId バッチID
     * @param scheduleParams スケジュール設定
     * @return 更新結果
     */
    @PutMapping("/batch/{batchId}/schedule")
    public ResponseEntity<?> updateBatchSchedule(
            @PathVariable String batchId,
            @RequestBody Map<String, Object> scheduleParams) {
        return ResponseEntity.ok(batchService.updateBatchSchedule(batchId, scheduleParams));
    }
    
    /**
     * バッチのパラメータ設定を更新する
     *
     * @param batchId バッチID
     * @param paramSettings パラメータ設定
     * @return 更新結果
     */
    @PutMapping("/batch/{batchId}/parameters")
    public ResponseEntity<?> updateBatchParameters(
            @PathVariable String batchId,
            @RequestBody Map<String, Object> paramSettings) {
        return ResponseEntity.ok(batchService.updateBatchParameters(batchId, paramSettings));
    }
    
    /**
     * バッチの有効/無効状態を変更する
     *
     * @param batchId バッチID
     * @param enabled 有効フラグ（true: 有効、false: 無効）
     * @return 更新結果
     */
    @PutMapping("/batch/{batchId}/status")
    public ResponseEntity<?> toggleBatchStatus(
            @PathVariable String batchId,
            @RequestBody Map<String, Boolean> status) {
        Boolean enabled = status.get("enabled");
        return ResponseEntity.ok(batchService.toggleBatchStatus(batchId, enabled != null ? enabled : true));
    }
    
    /**
     * 実行中のバッチを停止する
     *
     * @param executionId 実行ID
     * @return 停止結果
     */
    @PostMapping("/batch/execution/{executionId}/stop")
    public ResponseEntity<?> stopBatchExecution(@PathVariable String executionId) {
        return ResponseEntity.ok(batchService.stopBatchExecution(executionId));
    }
}