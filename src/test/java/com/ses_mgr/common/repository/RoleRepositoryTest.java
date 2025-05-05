package com.ses_mgr.common.repository;

import com.ses_mgr.common.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // 管理者ロールの作成
        adminRole = new Role();
        adminRole.setRoleId(UUID.randomUUID());
        adminRole.setRoleCode("ADMIN");
        adminRole.setName("システム管理者");
        adminRole.setRoleType("system");
        adminRole = roleRepository.save(adminRole);

        // ユーザーロールの作成
        userRole = new Role();
        userRole.setRoleId(UUID.randomUUID());
        userRole.setRoleCode("USER");
        userRole.setName("一般ユーザー");
        userRole.setRoleType("business");
        userRole = roleRepository.save(userRole);
    }

    @Test
    void findByRoleCode_ShouldReturnRole() {
        // When
        Optional<Role> foundRole = roleRepository.findByRoleCode("ADMIN");

        // Then
        assertTrue(foundRole.isPresent());
        assertEquals("システム管理者", foundRole.get().getName());
    }

    @Test
    void existsByRoleCode_ShouldReturnTrue() {
        // When
        boolean exists = roleRepository.existsByRoleCode("ADMIN");
        boolean notExists = roleRepository.existsByRoleCode("NONEXISTENT");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void findByRoleType_ShouldReturnMatchingRoles() {
        // When
        List<Role> systemRoles = roleRepository.findByRoleType("system");
        List<Role> businessRoles = roleRepository.findByRoleType("business");

        // Then
        assertEquals(1, systemRoles.size());
        assertEquals(1, businessRoles.size());
        assertEquals("ADMIN", systemRoles.get(0).getRoleCode());
        assertEquals("USER", businessRoles.get(0).getRoleCode());
    }

    @Test
    void searchByNameOrCode_ShouldReturnMatchingRoles() {
        // When
        Page<Role> adminResults = roleRepository.searchByNameOrCode("admin", PageRequest.of(0, 10));
        Page<Role> userResults = roleRepository.searchByNameOrCode("user", PageRequest.of(0, 10));
        Page<Role> systemResults = roleRepository.searchByNameOrCode("system", PageRequest.of(0, 10));

        // Then
        assertEquals(1, adminResults.getTotalElements());
        assertEquals(1, userResults.getTotalElements());
        assertEquals(0, systemResults.getTotalElements());  // roleTypeはコードにもnameにも含まれないため
    }
}