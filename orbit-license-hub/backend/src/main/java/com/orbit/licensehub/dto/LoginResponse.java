package com.orbit.licensehub.dto;

import com.orbit.licensehub.entity.UserRole;

public record LoginResponse(String accessToken, String username, UserRole role, Long tenantId) {}
