package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.Department;
import com.ses_mgr.common.entity.Role;
import com.ses_mgr.common.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Department testDepartment;
    private User testUser1;

    @BeforeEach
    void setUp() {
        // 部署の作成
        testDepartment = new Department();
        testDepartment.setDepartmentName("Test Department");
        testDepartment.setDepartmentCode("TST");
        testDepartment.setIsActive(true);
        testDepartment.setChildDepartments(Collections.emptySet());
        testDepartment.setUsers(Collections.emptySet());
        testDepartment = departmentRepository.save(testDepartment);

        // ユーザーの作成
        testUser1 = new User();
        testUser1.setUserId(UUID.randomUUID());
        testUser1.setLoginId("admin.user");
        testUser1.setEmail("admin@example.com");
        testUser1.setName("Admin User");
        testUser1.setDepartment(testDepartment);
        testUser1.setPasswordHash("hashedPassword");
        testUser1.setStatus("active");
        testUser1.setUserRoles(Collections.emptySet());
        testUser1 = userRepository.save(testUser1);

        User testUser2 = new User();
        testUser2.setUserId(UUID.randomUUID());
        testUser2.setLoginId("regular.user");
        testUser2.setEmail("user@example.com");
        testUser2.setName("Regular User");
        testUser2.setDepartment(testDepartment);
        testUser2.setPasswordHash("hashedPassword");
        testUser2.setStatus("active");
        testUser2.setUserRoles(Collections.emptySet());
        userRepository.save(testUser2);
    }

    @Test
    void findByLoginId_ShouldReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findByLoginId("admin.user");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("admin.user", foundUser.get().getLoginId());
        assertEquals("Admin User", foundUser.get().getName());
    }

    @Test
    void findByLoginId_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByLoginId("nonexistent.user");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("admin@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("admin@example.com", foundUser.get().getEmail());
        assertEquals("Admin User", foundUser.get().getName());
    }

    @Test
    void findByStatus_ShouldReturnMatchingUsers() {
        // Given
        User inactiveUser = new User();
        inactiveUser.setUserId(UUID.randomUUID());
        inactiveUser.setLoginId("inactive.user");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setName("Inactive User");
        inactiveUser.setPasswordHash("hashedPassword");
        inactiveUser.setStatus("inactive");
        inactiveUser.setUserRoles(Collections.emptySet());
        userRepository.save(inactiveUser);

        // When
        var activeUsers = userRepository.findByStatus("active");
        var inactiveUsers = userRepository.findByStatus("inactive");

        // Then
        assertEquals(2, activeUsers.size());
        assertEquals(1, inactiveUsers.size());
        assertEquals("inactive@example.com", inactiveUsers.get(0).getEmail());
    }

    @Test
    void searchByKeyword_ShouldReturnMatchingUsers() {
        // When
        Page<User> resultsForAdmin = userRepository.searchByKeyword("admin", PageRequest.of(0, 10));
        Page<User> resultsForExample = userRepository.searchByKeyword("example", PageRequest.of(0, 10));

        // Then
        assertEquals(1, resultsForAdmin.getTotalElements());
        assertEquals("admin@example.com", resultsForAdmin.getContent().get(0).getEmail());

        assertEquals(2, resultsForExample.getTotalElements());
    }
}