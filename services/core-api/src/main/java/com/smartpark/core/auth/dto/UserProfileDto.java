package com.smartpark.core.auth.dto;

import com.smartpark.core.auth.domain.User;
import java.util.Set;
import java.util.stream.Collectors;

public record UserProfileDto(
    Long id,
    String username,
    String email,
    String fullName,
    Set<String> roles
) {
    public static UserProfileDto fromEntity(User user) {
        Set<String> roleNames = user.getRoles().stream()
            .map(r -> r.getName().replace("ROLE_", ""))
            .collect(Collectors.toSet());
        return new UserProfileDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), roleNames);
    }
}
