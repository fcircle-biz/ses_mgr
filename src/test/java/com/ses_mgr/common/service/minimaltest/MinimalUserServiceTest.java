package com.ses_mgr.common.service.minimaltest;

import com.ses_mgr.common.repository.UserRepository;
import com.ses_mgr.common.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 最小構成での統合テスト
 * リポジトリをモック化することでエンティティの循環参照問題を回避
 */
@SpringBootTest
@ActiveProfiles("test")
public class MinimalUserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        // Spring コンテキストが正常にロードされるかテスト
        assertNotNull(userService);
        assertNotNull(userRepository);
    }
}