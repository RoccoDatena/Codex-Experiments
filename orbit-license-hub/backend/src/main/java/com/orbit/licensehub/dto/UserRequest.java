package com.orbit.licensehub.dto;

import com.orbit.licensehub.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record UserRequest(
    @NotBlank String username,
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotNull UserRole role,
    Long tenantId,
    Set<String> extraPermissions) {}
