package com.groupb.booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public IdempotencyService(
            StringRedisTemplate redis,
            @Value("${booking.idempotency.ttl-minutes:60}") long ttlMinutes) {
        this.redis = redis;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    /**
     * Attempts to acquire an idempotency lock for the given key.
     * Returns true if this is the first time (key was set), false if already seen.
     */
    public boolean tryAcquire(String idempotencyKey) {
        Boolean wasSet = redis.opsForValue()
                .setIfAbsent("idempotency:" + idempotencyKey, "1", ttl);
        return Boolean.TRUE.equals(wasSet);
    }

    /**
     * Releases an idempotency lock (used if booking creation fails and we want
     * to allow retry).
     */
    public void release(String idempotencyKey) {
        redis.delete("idempotency:" + idempotencyKey);
    }
}
