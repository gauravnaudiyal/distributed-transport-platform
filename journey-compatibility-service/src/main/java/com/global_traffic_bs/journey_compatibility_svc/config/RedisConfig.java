package com.global_traffic_bs.journey_compatibility_svc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * RedisTemplate configured for String keys and Long values.
     * Used for atomic capacity counter operations (INCR/DECR).
     */
    @Bean
    public RedisTemplate<String, Long> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Lua script for atomic: INCR + check capacity + set TTL if new key.
     * Returns the new counter value after increment.
     * KEYS[1] = capacity key
     * ARGV[1] = max capacity
     * ARGV[2] = TTL in seconds
     */
    @Bean
    public DefaultRedisScript<Long> incrementIfUnderCapacityScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
                local current = redis.call('INCR', KEYS[1])
                if current == 1 then
                    redis.call('EXPIRE', KEYS[1], ARGV[2])
                end
                if current > tonumber(ARGV[1]) then
                    redis.call('DECR', KEYS[1])
                    return -1
                end
                return current
                """);
        script.setResultType(Long.class);
        return script;
    }
}
