package com.orbit.licensehub.service;

import com.orbit.licensehub.dto.AssignLicenseRequest;
import com.orbit.licensehub.dto.LicenseResponse;
import com.orbit.licensehub.dto.PageResponse;
import com.orbit.licensehub.entity.ApplicationEntity;
import com.orbit.licensehub.entity.ApplicationStatus;
import com.orbit.licensehub.entity.LicenseEntity;
import com.orbit.licensehub.entity.LicenseStatus;
import com.orbit.licensehub.entity.TenantEntity;
import com.orbit.licensehub.entity.UserRole;
import com.orbit.licensehub.repository.LicenseRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LicenseService {

  private final LicenseRepository licenseRepository;
  private final TenantService tenantService;
  private final ApplicationService applicationService;
  private final AccessScopeService accessScopeService;
  private final PageMapperService pageMapperService;

  public LicenseService(
      LicenseRepository licenseRepository,
      TenantService tenantService,
      ApplicationService applicationService,
      AccessScopeService accessScopeService,
      PageMapperService pageMapperService) {
    this.licenseRepository = licenseRepository;
    this.tenantService = tenantService;
    this.applicationService = applicationService;
    this.accessScopeService = accessScopeService;
    this.pageMapperService = pageMapperService;
  }

  public PageResponse<LicenseResponse> list(
      int page, int size, String search, String sort, Long tenantId) {
    accessScopeService.requireAnyRole(
        UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN, UserRole.LICENSE_ADMIN, UserRole.LICENSE_VIEWER);

    Long scopedTenantId = accessScopeService.enforceTenantScope(tenantId);
    Pageable pageable = pageRequest(page, size, sort);

    Page<LicenseEntity> result;
    if (scopedTenantId != null) {
      TenantEntity tenant = tenantService.getEntity(scopedTenantId);
      result = licenseRepository.findByTenantAndDeviceCodeContainingIgnoreCase(tenant, search, pageable);
    } else {
      result = licenseRepository.findByDeviceCodeContainingIgnoreCase(search, pageable);
    }

    return pageMapperService.toResponse(result.map(this::toResponse));
  }

  public LicenseResponse assign(AssignLicenseRequest request) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN, UserRole.LICENSE_ADMIN);

    Long scopedTenantId = accessScopeService.enforceTenantScope(request.tenantId());
    TenantEntity tenant = tenantService.getEntity(scopedTenantId != null ? scopedTenantId : request.tenantId());
    ApplicationEntity application = applicationService.getEntity(request.applicationId());

    if (!application.getTenant().getId().equals(tenant.getId())) {
      throw new IllegalArgumentException("Application does not belong to tenant");
    }
    if (application.getStatus() != ApplicationStatus.ACTIVE) {
      throw new IllegalArgumentException("Application is disabled");
    }

    long used = licenseRepository.countByApplicationAndStatus(application, LicenseStatus.ASSIGNED);
    if (used >= application.getAllocatedLicenses()) {
      throw new IllegalArgumentException("No available allocated licenses for this application");
    }

    LicenseEntity entity = new LicenseEntity();
    entity.setTenant(tenant);
    entity.setApplication(application);
    entity.setDeviceCode(request.deviceCode());
    entity.setStatus(LicenseStatus.ASSIGNED);
    entity.setLastUsedAt(Instant.now());

    return toResponse(licenseRepository.save(entity));
  }

  public void deassign(Long id) {
    mutateStatus(id, LicenseStatus.FREE, true);
  }

  public void release(Long id) {
    mutateStatus(id, LicenseStatus.FREE, false);
  }

  private void mutateStatus(Long id, LicenseStatus status, boolean keepLastUsed) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN, UserRole.LICENSE_ADMIN);

    LicenseEntity entity = getEntity(id);
    accessScopeService.enforceTenantScope(entity.getTenant().getId());

    entity.setStatus(status);
    entity.setApplication(null);
    if (!keepLastUsed) {
      entity.setLastUsedAt(null);
    }

    licenseRepository.save(entity);
  }

  private LicenseEntity getEntity(Long id) {
    return licenseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("License not found"));
  }

  private LicenseResponse toResponse(LicenseEntity entity) {
    return new LicenseResponse(
        entity.getId(),
        entity.getTenant().getId(),
        entity.getApplication() != null ? entity.getApplication().getId() : null,
        entity.getDeviceCode(),
        entity.getStatus(),
        entity.getLastUsedAt());
  }

  private Pageable pageRequest(int page, int size, String sort) {
    String[] sortParts = sort.split(",");
    String property = sortParts.length > 0 ? sortParts[0] : "id";
    Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
        ? Sort.Direction.DESC
        : Sort.Direction.ASC;
    return PageRequest.of(page, size, Sort.by(direction, property));
  }
}
