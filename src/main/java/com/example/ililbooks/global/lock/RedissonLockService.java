package com.example.ililbooks.global.lock;

import com.example.ililbooks.global.exception.ErrorCode;
import com.example.ililbooks.global.exception.HandledException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockService {

    private final RedissonClient redissonClient;

    // 락 대기  시간 3초, 락 유지 시간 5초 설정
    private static final long WAIT_TIME  = 3L;
    private static final long LEASE_TIME = 5L;

    /*
     * 락을  획득한 상태에서 로직을 실행
     */
    public <T> T runWithLock(String lockKey, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new HandledException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HandledException(ErrorCode.LOCK_INTERRUPTED, "Redisson 락 처리 중 인터럽트 발생");
        } catch (Exception e) {
            throw new HandledException(ErrorCode.INTERNAL_SERVER_ERROR, "락 실행 중 예외 발생: " + e.getMessage());
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}