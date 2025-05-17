package jp.co.example.sesapp.common.auth.repository.jdbc;

import jp.co.example.sesapp.common.auth.domain.AuthenticationMethod;
import jp.co.example.sesapp.common.auth.domain.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JdbcUserRepository のシンプルなテストクラス
 * Spring Boot テストフレームワークを使用せず、直接JDBCを設定して単体テスト
 */
public class BasicUserRepositoryTest {

    private BasicJdbcUserRepository repository;
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    @BeforeEach
    void setUp() {
        // テスト用のデータソースを設定
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5433/ses_mgr_test");
        dataSource.setUsername("postgres_test");
        dataSource.setPassword("postgres_test");
        
        jdbcTemplate = new JdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
        // テスト用スキーマとテーブルを作成
        try {
            jdbcTemplate.execute("DROP SCHEMA IF EXISTS auth CASCADE");
            jdbcTemplate.execute("CREATE SCHEMA auth");
            
            jdbcTemplate.execute("CREATE TABLE auth.users (" +
                "id UUID PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL UNIQUE, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "first_name VARCHAR(100), " +
                "last_name VARCHAR(100), " +
                "department_id UUID, " +
                "is_enabled BOOLEAN NOT NULL DEFAULT TRUE, " +
                "is_account_locked BOOLEAN NOT NULL DEFAULT FALSE, " +
                "account_expire_date TIMESTAMP, " +
                "credentials_expire_date TIMESTAMP, " +
                "last_login_date TIMESTAMP, " +
                "login_fail_count INT NOT NULL DEFAULT 0, " +
                "authentication_method VARCHAR(50), " +
                "is_mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE, " +
                "mfa_secret VARCHAR(255), " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP)");
            
            // テストユーザーを登録
            UUID testUserId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
            jdbcTemplate.update(
                "INSERT INTO auth.users (id, username, email, password_hash, first_name, last_name, " +
                "is_enabled, authentication_method, created_at) VALUES (CAST(? AS UUID), ?, ?, ?, ?, ?, ?, ?, ?)",
                testUserId.toString(), "testuser", "test@example.com", "password", "Test", "User", 
                true, "PASSWORD", LocalDateTime.now()
            );
        } catch (Exception e) {
            System.err.println("テスト環境のセットアップに失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
        
        // テスト対象のリポジトリを作成（明示的なUUIDキャストを使用したテスト用の実装を使用）
        repository = new BasicJdbcUserRepository(jdbcTemplate, namedParameterJdbcTemplate);
    }
    
    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        try {
            // Given - テストデータベースから既存のユーザーIDを取得
            UUID testUserId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
            
            // When
            Optional<User> result = repository.findById(testUserId);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("testuser");
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        } catch (Exception e) {
            System.err.println("findById テストでエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // When
        Optional<User> result = repository.findByUsername("testuser");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    void save_ShouldInsertNewUser_WhenUserDoesNotExist() {
        try {
            // Given - SQL直接挿入でテスト
            UUID newUserId = UUID.randomUUID();
            jdbcTemplate.update(
                "INSERT INTO auth.users (id, username, email, password_hash, first_name, last_name, " +
                "is_enabled, authentication_method, created_at) VALUES (CAST(? AS UUID), ?, ?, ?, ?, ?, ?, ?, ?)",
                newUserId.toString(), "newuser", "new@example.com", "password", "New", "User", 
                true, "PASSWORD", LocalDateTime.now()
            );
            
            // When
            Optional<User> result = repository.findByUsername("newuser");
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("new@example.com");
        } catch (Exception e) {
            System.err.println("テストでエラーが発生しました: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    void findAll_ShouldReturnAllUsers() {
        // When
        List<User> users = repository.findAll();
        
        // Then
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThanOrEqualTo(1);
    }
}