package com.smartpark.core.auth.service;

import com.smartpark.core.auth.domain.Role;
import com.smartpark.core.auth.domain.User;
import com.smartpark.core.auth.dto.*;
import com.smartpark.core.auth.repository.RoleRepository;
import com.smartpark.core.auth.repository.UserRepository;
import com.smartpark.core.common.exception.BadRequestException;
import com.smartpark.core.common.exception.ResourceNotFoundException;
import com.smartpark.core.common.exception.UnauthorizedException;
import com.smartpark.core.security.JwtTokenProvider;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
        AuthenticationManager authenticationManager,
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider tokenProvider
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String accessToken = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(username);

        return AuthResponse.of(accessToken, refreshToken, tokenProvider.getExpirationMs(), UserProfileDto.fromEntity(user));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username is already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered: " + request.email());
        }

        User user = new User(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.fullName()
        );

        String targetRole = request.roleName() != null && !request.roleName().isBlank()
            ? (request.roleName().startsWith("ROLE_") ? request.roleName() : "ROLE_" + request.roleName())
            : "ROLE_CITY_PLANNER";

        Role role = roleRepository.findByName(targetRole)
            .orElseGet(() -> roleRepository.save(new Role(targetRole, "Assigned role: " + targetRole)));

        user.setRoles(Set.of(role));
        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String accessToken = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(savedUser.getUsername());

        return AuthResponse.of(accessToken, refreshToken, tokenProvider.getExpirationMs(), UserProfileDto.fromEntity(savedUser));
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.refreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(request.refreshToken());
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            username,
            null,
            user.getRoles().stream().map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getName())).toList()
        );

        String newAccessToken = tokenProvider.createToken(authentication);
        String newRefreshToken = tokenProvider.createRefreshToken(username);

        return AuthResponse.of(newAccessToken, newRefreshToken, tokenProvider.getExpirationMs(), UserProfileDto.fromEntity(user));
    }

    public UserProfileDto getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return UserProfileDto.fromEntity(user);
    }
}
