package com.orbit.licensehub.service;

import com.orbit.licensehub.dto.ApplicationCredentialsResponse;
import com.orbit.licensehub.dto.ApplicationRequest;
import com.orbit.licensehub.dto.ApplicationResponse;
import com.orbit.licensehub.dto.PageResponse;
import com.orbit.licensehub.entity.ApplicationEntity;
import com.orbit.licensehub.entity.ApplicationStatus;
import com.orbit.licensehub.entity.TenantEntity;
import com.orbit.licensehub.entity.UserRole;
import com.orbit.licensehub.repository.ApplicationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final TenantService tenantService;
  private final AccessScopeService accessScopeService;
  private final PageMapperService pageMapperService;
  private final CryptoService cryptoService;

  public ApplicationService(
      ApplicationRepository applicationRepository,
      TenantService tenantService,
      AccessScopeService accessScopeService,
      PageMapperService pageMapperService,
      CryptoService cryptoService) {
    this.applicationRepository = applicationRepository;
    this.tenantService = tenantService;
    this.accessScopeService = accessScopeService;
    this.pageMapperService = pageMapperService;
    this.cryptoService = cryptoService;
  }

  public PageResponse<ApplicationResponse> list(
      int page, int size, String search, String sort, Long tenantId) {
    Long scopedTenantId = accessScopeService.enforceTenantScope(tenantId);
    Pageable pageable = pageRequest(page, size, sort);

    Page<ApplicationEntity> result;
    if (scopedTenantId != null) {
      TenantEntity tenant = tenantService.getEntity(scopedTenantId);
      result = applicationRepository.findByTenantAndNameContainingIgnoreCase(tenant, search, pageable);
    } else {
      result = applicationRepository.findByNameContainingIgnoreCase(search, pageable);
    }

    return pageMapperService.toResponse(result.map(this::toResponse));
  }

  @Transactional
  public ApplicationCredentialsResponse create(ApplicationRequest request) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN);

    Long scopedTenantId = accessScopeService.enforceTenantScope(request.tenantId());
    TenantEntity tenant = tenantService.getEntity(scopedTenantId != null ? scopedTenantId : request.tenantId());

    int available = tenant.getPurchasedLicenses() - tenant.getAssignedLicenses();
    if (request.allocatedLicenses() > available) {
      throw new IllegalArgumentException("Not enough available tenant licenses");
    }

    String apiKey = randomToken(24);
    String secret = randomToken(32);

    ApplicationEntity entity = new ApplicationEntity();
    entity.setTenant(tenant);
    entity.setName(request.name());
    entity.setLogoUrl(request.logoUrl());
    entity.setBaseColor(request.baseColor());
    entity.setEnvironment(request.environment());
    entity.setP12FileRef(request.p12FileRef());
    entity.setP12PasswordEncrypted(cryptoService.encrypt(request.p12Password()));
    entity.setRetentionMinutes(request.retentionMinutes());
    entity.setPublicKeyPem(request.publicKeyPem());
    entity.setAllocatedLicenses(request.allocatedLicenses());
    entity.setClientApiKey(apiKey);
    entity.setClientSecretEncrypted(cryptoService.encrypt(secret));
    entity.setStatus(ApplicationStatus.ACTIVE);

    ApplicationEntity saved = applicationRepository.save(entity);

    tenant.setAssignedLicenses(tenant.getAssignedLicenses() + request.allocatedLicenses());
    tenantService.save(tenant);

    return new ApplicationCredentialsResponse(saved.getId(), apiKey, secret);
  }

  @Transactional
  public ApplicationResponse update(Long id, ApplicationRequest request) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN);

    ApplicationEntity entity = getEntity(id);
    Long scopedTenantId = accessScopeService.enforceTenantScope(entity.getTenant().getId());
    if (scopedTenantId != null && !scopedTenantId.equals(entity.getTenant().getId())) {
      throw new IllegalArgumentException("Cross-tenant access denied");
    }

    TenantEntity tenant = entity.getTenant();
    int delta = request.allocatedLicenses() - entity.getAllocatedLicenses();
    int available = tenant.getPurchasedLicenses() - tenant.getAssignedLicenses();
    if (delta > available) {
      throw new IllegalArgumentException("Not enough available tenant licenses");
    }

    entity.setName(request.name());
    entity.setLogoUrl(request.logoUrl());
    entity.setBaseColor(request.baseColor());
    entity.setP12FileRef(request.p12FileRef());
    entity.setP12PasswordEncrypted(cryptoService.encrypt(request.p12Password()));
    entity.setRetentionMinutes(request.retentionMinutes());
    entity.setPublicKeyPem(request.publicKeyPem());
    entity.setAllocatedLicenses(request.allocatedLicenses());

    tenant.setAssignedLicenses(tenant.getAssignedLicenses() + delta);
    tenantService.save(tenant);

    return toResponse(applicationRepository.save(entity));
  }

  public void disable(Long id) {
    toggleStatus(id, ApplicationStatus.DISABLED);
  }

  public void enable(Long id) {
    toggleStatus(id, ApplicationStatus.ACTIVE);
  }

  public ApplicationCredentialsResponse credentials(Long id) {
    ApplicationEntity entity = getEntity(id);
    accessScopeService.enforceTenantScope(entity.getTenant().getId());

    return new ApplicationCredentialsResponse(
        entity.getId(), entity.getClientApiKey(), cryptoService.decrypt(entity.getClientSecretEncrypted()));
  }

  public ApplicationEntity getEntity(Long id) {
    return applicationRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Application not found"));
  }

  private void toggleStatus(Long id, ApplicationStatus status) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN);

    ApplicationEntity entity = getEntity(id);
    accessScopeService.enforceTenantScope(entity.getTenant().getId());
    entity.setStatus(status);
    applicationRepository.save(entity);
  }

  private ApplicationResponse toResponse(ApplicationEntity entity) {
    return new ApplicationResponse(
        entity.getId(),
        entity.getTenant().getId(),
        entity.getName(),
        entity.getLogoUrl(),
        entity.getBaseColor(),
        entity.getEnvironment(),
        entity.getRetentionMinutes(),
        entity.getPublicKeyPem(),
        entity.getAllocatedLicenses(),
        entity.getStatus());
  }

  private Pageable pageRequest(int page, int size, String sort) {
    String[] sortParts = sort.split(",");
    String property = sortParts.length > 0 ? sortParts[0] : "id";
    Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
        ? Sort.Direction.DESC
        : Sort.Direction.ASC;
    return PageRequest.of(page, size, Sort.by(direction, property));
  }

  private String randomToken(int byteSize) {
    byte[] bytes = new byte[byteSize];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
