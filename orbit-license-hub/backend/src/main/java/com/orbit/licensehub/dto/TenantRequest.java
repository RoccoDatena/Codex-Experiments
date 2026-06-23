package com.orbit.licensehub.dto;

import com.orbit.licensehub.entity.TenantStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TenantRequest(
    @NotBlank String name,
    @NotNull @Min(0) Integer purchasedLicenses,
    @NotNull TenantStatus status) {}
