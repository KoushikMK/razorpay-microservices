package com.coding.razorpay.common_lib.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class FixedWindowRateLimiter implements RateLimiter{

    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds) {

        String redisKey = "ratelimit:fixed:"+key;

        Long count  = redis.opsForValue().increment(redisKey);

        if(count == null) return RateLimitResult.allowed(maxRequestAllowed); // redis unavailable

        if (count == 1) {
            redis.expire(redisKey, Duration.ofSeconds(windowSeconds));   //first time this expire is creating
        }

        if (count > maxRequestAllowed) {
            Long ttl = redis.getExpire(redisKey, TimeUnit.SECONDS);   //getting expire
            int retryAfter = (ttl != null & ttl > 0) ? ttl.intValue(): (int) windowSeconds;   //we can retunr this time to user when you can hit
            return RateLimitResult.denied(retryAfter);
        }

        return RateLimitResult.allowed((int) (maxRequestAllowed - count));
    }
}
