package com.smartpark.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private JwtAuthGatewayFilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        filterFactory = new JwtAuthGatewayFilterFactory(SECRET);
    }

    @Test
    void shouldReturnUnauthorizedWhenHeaderMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/zones").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filterFactory.apply(new JwtAuthGatewayFilterFactory.Config()).filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenInvalid() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/zones")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-payload")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        filterFactory.apply(new JwtAuthGatewayFilterFactory.Config()).filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldPassValidTokenAndMutateHeaders() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String validJwt = Jwts.builder()
                .subject("city_planner")
                .claim("role", "ROLE_CITY_PLANNER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/zones")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwt)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filterFactory.apply(new JwtAuthGatewayFilterFactory.Config()).filter(exchange, chain).block();

        verify(chain, times(1)).filter(argThat(mutatedExchange -> {
            String roleHeader = mutatedExchange.getRequest().getHeaders().getFirst("X-User-Role");
            String usernameHeader = mutatedExchange.getRequest().getHeaders().getFirst("X-User-Username");
            return "ROLE_CITY_PLANNER".equals(roleHeader) && "city_planner".equals(usernameHeader);
        }));
    }
}
