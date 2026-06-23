package com.orbit.licensehub.dto;

import com.orbit.licensehub.entity.UserRole;
import java.util.Set;

public record UserResponse(
    Long id,
    String username,
    String email,
    UserRole role,
    Long tenantId,
    Set<String> extraPermissions,
    boolean enabled) {}
