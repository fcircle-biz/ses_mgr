package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department parentDepartment;
    private Department childDepartment;

    @BeforeEach
    void setUp() {
        // 親部署の作成
        parentDepartment = new Department();
        parentDepartment.setDepartmentName("本社");
        parentDepartment.setDepartmentCode("HQ");
        parentDepartment.setIsActive(true);
        parentDepartment = departmentRepository.save(parentDepartment);

        // 子部署の作成
        childDepartment = new Department();
        childDepartment.setDepartmentName("営業部");
        childDepartment.setDepartmentCode("SALES");
        childDepartment.setIsActive(true);
        childDepartment.setParentDepartment(parentDepartment);
        childDepartment = departmentRepository.save(childDepartment);
    }

    @Test
    void findByDepartmentCode_ShouldReturnDepartment() {
        // When
        Optional<Department> foundDepartment = departmentRepository.findByDepartmentCode("HQ");

        // Then
        assertTrue(foundDepartment.isPresent());
        assertEquals("本社", foundDepartment.get().getDepartmentName());
    }

    @Test
    void findByParentDepartmentIsNull_ShouldReturnRootDepartments() {
        // When
        List<Department> rootDepartments = departmentRepository.findByParentDepartmentIsNull();

        // Then
        assertFalse(rootDepartments.isEmpty());
        assertEquals(1, rootDepartments.size());
        assertEquals("本社", rootDepartments.get(0).getDepartmentName());
    }

    @Test
    void findByParentDepartmentDepartmentId_ShouldReturnChildDepartments() {
        // When
        List<Department> childDepartments = departmentRepository.findByParentDepartmentDepartmentId(parentDepartment.getDepartmentId());

        // Then
        assertFalse(childDepartments.isEmpty());
        assertEquals(1, childDepartments.size());
        assertEquals("営業部", childDepartments.get(0).getDepartmentName());
    }

    @Test
    void findByIsActive_ShouldReturnActiveDepartments() {
        // When
        List<Department> activeDepartments = departmentRepository.findByIsActive(true);

        // Then
        assertEquals(2, activeDepartments.size());
        
        // 非アクティブな部署を追加
        Department inactiveDepartment = new Department();
        inactiveDepartment.setDepartmentName("廃止部門");
        inactiveDepartment.setDepartmentCode("INACTIVE");
        inactiveDepartment.setIsActive(false);
        departmentRepository.save(inactiveDepartment);
        
        // 再度検索
        activeDepartments = departmentRepository.findByIsActive(true);
        List<Department> inactiveDepartments = departmentRepository.findByIsActive(false);
        
        assertEquals(2, activeDepartments.size());
        assertEquals(1, inactiveDepartments.size());
        assertEquals("廃止部門", inactiveDepartments.get(0).getDepartmentName());
    }
}