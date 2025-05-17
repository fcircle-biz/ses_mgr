package jp.co.example.sesapp.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

/**
 * トランザクションの監視と記録を行うAspect
 * @Transactionalアノテーションがついたメソッドの実行時間とステータスをログに記録
 */
@Aspect
@Component
public class TransactionLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(TransactionLoggingAspect.class);

    /**
     * @Transactionalアノテーションがついたすべてのメソッドに対してアドバイスを実行
     * トランザクションの開始、終了、実行時間、成功/失敗をログに記録
     *
     * @param joinPoint ジョインポイント
     * @param transactional トランザクションアノテーション
     * @return メソッドの実行結果
     * @throws Throwable メソッド実行中の例外
     */
    @Around("@annotation(transactional)")
    public Object aroundTransactionalMethod(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        StopWatch stopWatch = new StopWatch();
        
        boolean readOnly = transactional.readOnly();
        logger.debug("トランザクション開始: {} [読取専用: {}]", methodName, readOnly);
        
        try {
            stopWatch.start();
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            logger.debug("トランザクション正常終了: {} [処理時間: {}ms]", 
                    methodName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Throwable t) {
            stopWatch.stop();
            logger.error("トランザクション異常終了: {} [処理時間: {}ms, 例外: {}]", 
                    methodName, stopWatch.getTotalTimeMillis(), t.getMessage(), t);
            throw t;
        }
    }
}