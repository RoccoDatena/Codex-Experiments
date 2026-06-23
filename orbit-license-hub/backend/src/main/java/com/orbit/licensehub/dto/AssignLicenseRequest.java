package com.orbit.licensehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignLicenseRequest(
    @NotNull Long tenantId, @NotNull Long applicationId, @NotBlank String deviceCode) {}
