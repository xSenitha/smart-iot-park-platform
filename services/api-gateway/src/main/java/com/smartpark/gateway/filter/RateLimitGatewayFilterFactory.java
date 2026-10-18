package com.smartpark.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitGatewayFilterFactory extends AbstractGatewayFilterFactory<RateLimitGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(RateLimitGatewayFilterFactory.class);

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown-client";

            TokenBucket bucket = buckets.computeIfAbsent(clientIp, k -> new TokenBucket(config.getCapacity(), config.getRefillPerSecond()));

            if (bucket.tryConsume()) {
                return chain.filter(exchange);
            } else {
                log.warn("Rate limit exceeded for client IP: {}", clientIp);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().add("Retry-After", "1");
                return exchange.getResponse().setComplete();
            }
        };
    }

    private static class TokenBucket {
        private final int capacity;
        private final double refillRatePerSecond;
        private double availableTokens;
        private Instant lastRefill;

        public TokenBucket(int capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
            this.availableTokens = capacity;
            this.lastRefill = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (availableTokens >= 1.0) {
                availableTokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            long millisPassed = Math.max(0, java.time.Duration.between(lastRefill, now).toMillis());
            double tokensToAdd = (millisPassed / 1000.0) * refillRatePerSecond;
            this.availableTokens = Math.min(capacity, this.availableTokens + tokensToAdd);
            this.lastRefill = now;
        }
    }

    public static class Config {
        private int capacity = 100;
        private double refillPerSecond = 50.0;

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public double getRefillPerSecond() {
            return refillPerSecond;
        }

        public void setRefillPerSecond(double refillPerSecond) {
            this.refillPerSecond = refillPerSecond;
        }
    }
}
