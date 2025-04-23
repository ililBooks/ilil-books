package com.example.ililbooks.global.redis.lock;

import com.example.ililbooks.global.exception.ErrorCode;
import com.example.ililbooks.global.exception.HandledException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.example.ililbooks.global.exception.ErrorMessage.REDISSON_LOCK_FAILED;
import static com.example.ililbooks.global.exception.ErrorMessage.REDISSON_LOCK_INTERRUPTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockService {

    private final RedissonClient redissonClient;

    // 락 대기  시간 3초, 락 유지 시간 5초 설정
    private static final long WAIT_TIME  = 3L;
    private static final long LEASE_TIME = 5L;

    /*/ 락을  획득한 상태에서 로직을 실행 */
    public <T> T runWithLock(String lockKey, Supplier<T> supplier) {
        return runWithLockInternal(lockKey, supplier, null);
    }

    /*/ 락 획득 실패 시 fallback 로직 실행 */
    public <T> T runWithLockOrElse(String lockKey, Supplier<T> supplier, Supplier<T> fallback) {
        return runWithLockInternal(lockKey, supplier, fallback);
    }

    /*/ 내부 공통 처리 로직 */
    private <T> T runWithLockInternal(String lockKey, Supplier<T> supplier, Supplier<T> fallback) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            log.info("[LOCK START] key={}, thread={}", lockKey, Thread.currentThread().getName());
            isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

            if (!isLocked) {
                log.warn("[LOCK FAIL] key={} - 락 획득 실패", lockKey);
                if (fallback != null) {
                    return fallback.get();
                }
                throw new HandledException(ErrorCode.BAD_REQUEST, REDISSON_LOCK_FAILED.getMessage() + " - key=" + lockKey);
            }

            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HandledException(ErrorCode.INTERNAL_SERVER_ERROR, REDISSON_LOCK_INTERRUPTED.getMessage() + " - key=" + lockKey);
        } catch (Exception e) {
            throw new HandledException(ErrorCode.INTERNAL_SERVER_ERROR, "락 처리 예외 - key=" + lockKey + ", cause=" + e.getMessage());
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[LOCK END] key={}, thread={}", lockKey, Thread.currentThread().getName());
            }
        }
    }
}