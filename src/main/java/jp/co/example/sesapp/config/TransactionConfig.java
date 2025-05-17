package jp.co.example.sesapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * トランザクション管理の設定クラス
 * Spring標準のトランザクション管理機能を利用してSES業務システムでのトランザクション制御を行う
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    private static final Logger logger = LoggerFactory.getLogger(TransactionConfig.class);

    /**
     * トランザクションマネージャーを設定
     * 
     * @param dataSource メインデータソース
     * @return データソーストランザクションマネージャー
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        logger.info("トランザクションマネージャーを初期化");
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * プログラム的なトランザクション制御に使用するトランザクションテンプレートを設定
     * 
     * @param transactionManager トランザクションマネージャー
     * @return トランザクションテンプレート
     */
    @Bean
    public TransactionTemplate transactionTemplate(
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
        logger.info("トランザクションテンプレートを初期化");
        return new TransactionTemplate(transactionManager);
    }
}