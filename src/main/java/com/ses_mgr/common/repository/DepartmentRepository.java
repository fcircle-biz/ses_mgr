package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    // 基本的なクエリメソッド
    Optional<Department> findByDepartmentCode(String departmentCode);
    
    boolean existsByDepartmentCode(String departmentCode);
    
    List<Department> findByIsActive(boolean isActive);
    
    // 特定の親部署に所属する部署の取得
    List<Department> findByParentDepartmentDepartmentId(Integer parentDepartmentId);
    
    // 特定の部署とその子孫部署をすべて取得
    @Query("SELECT d FROM Department d WHERE d.departmentId = :departmentId OR " +
           "d.parentDepartment.departmentId = :departmentId OR " +
           "d.parentDepartment.parentDepartment.departmentId = :departmentId")
    List<Department> findDepartmentWithDescendants(@Param("departmentId") Integer departmentId);
    
    // ルート部署（親部署がnull）の取得
    List<Department> findByParentDepartmentIsNull();
    
    // 名前またはコードによる検索
    @Query("SELECT d FROM Department d WHERE " +
           "LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.departmentCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Department> searchByNameOrCode(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // 部署名による検索
    Optional<Department> findByDepartmentName(String departmentName);
    
    // 階層付きの部署全取得（深さ優先）
    @Query(value = "WITH RECURSIVE dept_tree AS (" +
                  "SELECT d.*, 0 AS level, CAST(d.department_name AS VARCHAR(1000)) AS path " +
                  "FROM departments d " +
                  "WHERE d.parent_department_id IS NULL " +
                  "UNION ALL " +
                  "SELECT d.*, dt.level + 1, CAST(CONCAT(dt.path, ' > ', d.department_name) AS VARCHAR(1000)) " +
                  "FROM departments d " +
                  "JOIN dept_tree dt ON d.parent_department_id = dt.department_id" +
                  ") " +
                  "SELECT * FROM dept_tree ORDER BY path", 
           nativeQuery = true)
    List<Object[]> findAllWithHierarchy();
}