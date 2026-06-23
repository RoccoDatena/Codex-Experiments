package com.orbit.licensehub.dto;

import com.orbit.licensehub.entity.ApplicationEnvironment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
    @NotNull Long tenantId,
    @NotBlank String name,
    String logoUrl,
    @NotBlank String baseColor,
    @NotNull ApplicationEnvironment environment,
    String p12FileRef,
    @NotBlank String p12Password,
    @NotNull @Min(1) Integer retentionMinutes,
    @NotBlank String publicKeyPem,
    @NotNull @Min(0) Integer allocatedLicenses) {}
