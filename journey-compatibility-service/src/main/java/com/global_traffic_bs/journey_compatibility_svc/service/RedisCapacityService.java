package com.global_traffic_bs.journey_compatibility_svc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCapacityService {

    private final RedisTemplate<String, Long> redisTemplate;
    private final DefaultRedisScript<Long> incrementIfUnderCapacityScript;

    @Value("${app.route.time-window-hours:1}")
    private int timeWindowHours;

    /**
     * Attempts to reserve a slot atomically in Redis.
     *
     * TTL Strategy (event-time based):
     * The key expires shortly after the travel window itself closes, NOT 65 minutes
     * after the first booking lands. This means all advance bookings for the same
     * travel slot (e.g., booked 2 days early) share and reuse the same Redis key
     * for its entire valid lifetime, eliminating premature cache misses.
     *
     * @return the new slot count (1-based) on success, or -1 if at/over capacity.
     *         Throws on Redis error so that the circuit breaker can catch it.
     */
    public long tryReserveSlot(String routeId, LocalDate travelDate, LocalTime travelTime,
                                String vehicleType, int maxCapacity) {
        String key = buildCapacityKey(routeId, travelDate, travelTime, vehicleType);

        long ttlSeconds = computeTtlSeconds(travelDate, travelTime);
        if (ttlSeconds <= 0) {
            // The travel window has already passed — reject without touching Redis
            log.warn("Reject: travel window has already passed for key: {}", key);
            return -1L;
        }

        log.debug("Attempting to reserve slot for key: {} (TTL={}s)", key, ttlSeconds);

        Long result = redisTemplate.execute(
                incrementIfUnderCapacityScript,
                Collections.singletonList(key),
                (long) maxCapacity,
                ttlSeconds
        );

        if (result == null) {
            throw new RuntimeException("Redis returned null for key: " + key);
        }

        if (result == -1L) {
            log.info("Capacity full for key: {} (max={})", key, maxCapacity);
            return -1L;
        }

        log.info("Slot reserved for key: {} — current load: {}/{}", key, result, maxCapacity);
        return result;
    }

    /**
     * Releases a previously reserved slot (used on downstream failure/rollback).
     */
    public void releaseSlot(String routeId, LocalDate travelDate, LocalTime travelTime, String vehicleType) {
        String key = buildCapacityKey(routeId, travelDate, travelTime, vehicleType);
        redisTemplate.opsForValue().decrement(key);
        log.info("Released slot for key: {}", key);
    }

    /**
     * Gets the current counter value for a route+window. Used by reconciliation.
     */
    public Long getCurrentCount(String routeId, LocalDate travelDate, LocalTime travelTime, String vehicleType) {
        String key = buildCapacityKey(routeId, travelDate, travelTime, vehicleType);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Gets the cached max capacity policy for a route+window.
     */
    public Integer getPolicyCapacity(String routeId, LocalDate travelDate, LocalTime travelTime, String vehicleType) {
        String key = buildPolicyKey(routeId, travelDate, travelTime, vehicleType);
        Long val = redisTemplate.opsForValue().get(key);
        return val != null ? val.intValue() : null;
    }

    /**
     * Caches the max capacity policy for a route+window.
     */
    public void setPolicyCapacity(String routeId, LocalDate travelDate, LocalTime travelTime, String vehicleType, int maxCapacity) {
        String key = buildPolicyKey(routeId, travelDate, travelTime, vehicleType);
        long ttlSeconds = computeTtlSeconds(travelDate, travelTime);
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, (long) maxCapacity, java.time.Duration.ofSeconds(ttlSeconds));
            log.debug("Cached policy for key {}: maxCapacity={}", key, maxCapacity);
        }
    }

    /**
     * Forcefully sets the counter (used by reconciliation to correct drift).
     * Re-applies the event-time TTL so the reconciliation write does not
     * accidentally grant the key an infinite lifetime.
     */
    public void setCount(String routeId, LocalDate travelDate, LocalTime travelTime, String vehicleType, long count) {
        String key = buildCapacityKey(routeId, travelDate, travelTime, vehicleType);
        long ttlSeconds = computeTtlSeconds(travelDate, travelTime);
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, count,
                    java.time.Duration.ofSeconds(ttlSeconds));
        } else {
            // Window already passed — delete the key rather than writing a stale value
            redisTemplate.delete(key);
        }
        log.info("Reconciliation: reset key {} to {} (TTL={}s)", key, count, ttlSeconds);
    }

    /**
     * Key format: capacity:{routeId}:{date}:{hour}:{vehicleType}
     * Hours are bucketed by time window to group requests in the same window.
     */
    public String buildCapacityKey(String routeId, LocalDate travelDate, LocalTime travelTime, String vehicleType) {
        int bucket = (travelTime.getHour() / timeWindowHours) * timeWindowHours;
        return String.format("capacity:%s:%s:%02d:%s", routeId, travelDate, bucket, vehicleType.toUpperCase());
    }

    /**
     * Key format: policy:{routeId}:{date}:{hour}:{vehicleType}
     */
    public String buildPolicyKey(String routeId, LocalDate travelDate, LocalTime travelTime, String vehicleType) {
        int bucket = (travelTime.getHour() / timeWindowHours) * timeWindowHours;
        return String.format("policy:%s:%s:%02d:%s", routeId, travelDate, bucket, vehicleType.toUpperCase());
    }

    /**
     * Computes a TTL (in seconds) anchored to the END of the travel window, not to now.
     *
     * Formula:
     *   windowEnd  = travelDate + floor(travelHour / windowSize) * windowSize + windowSize hours
     *   expiryAt   = windowEnd + 5-minute buffer (for in-flight requests to settle)
     *   TTL        = expiryAt - LocalDateTime.now()
     *
     * Example (timeWindowHours=1):
     *   travelDate=2026-03-10, travelTime=12:45 →
     *   bucket=12, windowEnd=2026-03-10T13:00, expiryAt=2026-03-10T13:05
     *   If booking is made on 2026-03-08 12:00 → TTL ≈ 49h5m (176,700s)
     *   All bookings for that slot share this key until 2026-03-10T13:05.
     *
     * @return TTL in seconds; negative means the window has already passed.
     */
    private long computeTtlSeconds(LocalDate travelDate, LocalTime travelTime) {
        int bucket = (travelTime.getHour() / timeWindowHours) * timeWindowHours;
        LocalDateTime windowEnd = LocalDateTime.of(travelDate, LocalTime.of(bucket, 0))
                .plusHours(timeWindowHours);
        LocalDateTime expiryAt = windowEnd.plusMinutes(5); // 5-minute buffer
        return Duration.between(LocalDateTime.now(), expiryAt).getSeconds();
    }
}
