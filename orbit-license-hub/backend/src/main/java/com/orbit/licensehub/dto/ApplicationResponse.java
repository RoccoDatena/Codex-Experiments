package com.orbit.licensehub.dto;

import com.orbit.licensehub.entity.ApplicationEnvironment;
import com.orbit.licensehub.entity.ApplicationStatus;

public record ApplicationResponse(
    Long id,
    Long tenantId,
    String name,
    String logoUrl,
    String baseColor,
    ApplicationEnvironment environment,
    Integer retentionMinutes,
    String publicKeyPem,
    Integer allocatedLicenses,
    ApplicationStatus status) {}
