package com.smartpark.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitGatewayFilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        filterFactory = new RateLimitGatewayFilterFactory();
    }

    @Test
    void shouldAllowRequestsWithinCapacity() {
        RateLimitGatewayFilterFactory.Config config = new RateLimitGatewayFilterFactory.Config();
        config.setCapacity(5);
        config.setRefillPerSecond(1.0);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/zones")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 50000))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filterFactory.apply(config).filter(exchange, chain).block();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void shouldBlockWhenRateLimitExceeded() {
        RateLimitGatewayFilterFactory.Config config = new RateLimitGatewayFilterFactory.Config();
        config.setCapacity(2);
        config.setRefillPerSecond(0.0); // No refill

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // First 2 consume capacity
        for (int i = 0; i < 2; i++) {
            MockServerHttpRequest req = MockServerHttpRequest.get("/api/v1/zones")
                    .remoteAddress(new InetSocketAddress("10.0.0.50", 40000))
                    .build();
            MockServerWebExchange ex = MockServerWebExchange.from(req);
            filterFactory.apply(config).filter(ex, chain).block();
        }

        // 3rd request should receive 429
        MockServerHttpRequest thirdReq = MockServerHttpRequest.get("/api/v1/zones")
                .remoteAddress(new InetSocketAddress("10.0.0.50", 40000))
                .build();
        MockServerWebExchange thirdExchange = MockServerWebExchange.from(thirdReq);

        filterFactory.apply(config).filter(thirdExchange, chain).block();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, thirdExchange.getResponse().getStatusCode());
        assertEquals("1", thirdExchange.getResponse().getHeaders().getFirst("Retry-After"));
    }
}
