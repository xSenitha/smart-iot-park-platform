package com.smartpark.core.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartpark.core.auth.domain.Role;
import com.smartpark.core.auth.domain.User;
import com.smartpark.core.auth.dto.AuthResponse;
import com.smartpark.core.auth.dto.LoginRequest;
import com.smartpark.core.auth.dto.RegisterRequest;
import com.smartpark.core.auth.repository.RoleRepository;
import com.smartpark.core.auth.repository.UserRepository;
import com.smartpark.core.auth.service.AuthService;
import com.smartpark.core.common.exception.BadRequestException;
import com.smartpark.core.security.JwtTokenProvider;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, userRepository, roleRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("admin", "Admin123!");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        User user = new User("admin", "admin@smartpark.local", "hash", "Admin User");
        Role role = new Role("ROLE_ADMIN", "Admin");
        user.setRoles(Set.of(role));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(tokenProvider.createToken(authentication)).thenReturn("jwt.access.token");
        when(tokenProvider.createRefreshToken("admin")).thenReturn("jwt.refresh.token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt.access.token");
        assertThat(response.refreshToken()).isEqualTo("jwt.refresh.token");
        assertThat(response.user().username()).isEqualTo("admin");
    }

    @Test
    void testRegister_UsernameTaken_ThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest("admin", "admin@new.local", "password123", "Admin Name", "ADMIN");
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Username is already taken");
    }
}
