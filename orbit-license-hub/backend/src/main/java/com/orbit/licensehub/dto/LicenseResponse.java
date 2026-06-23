package com.orbit.licensehub.dto;

import com.orbit.licensehub.entity.LicenseStatus;
import java.time.Instant;

public record LicenseResponse(
    Long id,
    Long tenantId,
    Long applicationId,
    String deviceCode,
    LicenseStatus status,
    Instant lastUsedAt) {}
