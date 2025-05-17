package jp.co.example.sesapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * SES管理システムのメインアプリケーションクラス.
 * Spring Bootアプリケーションのエントリーポイントとなります。
 */
@SpringBootApplication
@EnableJdbcRepositories
public class SesApplication {

    /**
     * アプリケーションのメインメソッド.
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(SesApplication.class, args);
    }
}