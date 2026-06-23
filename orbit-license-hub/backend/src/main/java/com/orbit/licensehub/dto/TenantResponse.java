package com.orbit.licensehub.dto;

import com.orbit.licensehub.entity.TenantStatus;

public record TenantResponse(
    Long id,
    String name,
    Integer purchasedLicenses,
    Integer assignedLicenses,
    Integer availableLicenses,
    TenantStatus status) {}
