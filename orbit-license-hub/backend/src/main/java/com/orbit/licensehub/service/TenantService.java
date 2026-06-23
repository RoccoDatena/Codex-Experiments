package com.orbit.licensehub.service;

import com.orbit.licensehub.dto.PageResponse;
import com.orbit.licensehub.dto.TenantRequest;
import com.orbit.licensehub.dto.TenantResponse;
import com.orbit.licensehub.entity.TenantEntity;
import com.orbit.licensehub.entity.UserRole;
import com.orbit.licensehub.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

  private final TenantRepository tenantRepository;
  private final AccessScopeService accessScopeService;
  private final PageMapperService pageMapperService;

  public TenantService(
      TenantRepository tenantRepository,
      AccessScopeService accessScopeService,
      PageMapperService pageMapperService) {
    this.tenantRepository = tenantRepository;
    this.accessScopeService = accessScopeService;
    this.pageMapperService = pageMapperService;
  }

  public PageResponse<TenantResponse> list(int page, int size, String search, String sort) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN);
    Pageable pageable = pageRequest(page, size, sort);
    Page<TenantEntity> items = tenantRepository.findByNameContainingIgnoreCase(search, pageable);
    return pageMapperService.toResponse(items.map(this::toResponse));
  }

  public TenantResponse create(TenantRequest request) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN);

    TenantEntity entity = new TenantEntity();
    entity.setName(request.name());
    entity.setPurchasedLicenses(request.purchasedLicenses());
    entity.setAssignedLicenses(0);
    entity.setStatus(request.status());

    return toResponse(tenantRepository.save(entity));
  }

  public TenantResponse update(Long id, TenantRequest request) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN);

    TenantEntity entity = getEntity(id);
    if (request.purchasedLicenses() < entity.getAssignedLicenses()) {
      throw new IllegalArgumentException("Purchased licenses cannot be lower than assigned licenses");
    }

    entity.setName(request.name());
    entity.setPurchasedLicenses(request.purchasedLicenses());
    entity.setStatus(request.status());

    return toResponse(tenantRepository.save(entity));
  }

  public TenantEntity getEntity(Long id) {
    return tenantRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
  }

  public void save(TenantEntity tenant) {
    tenantRepository.save(tenant);
  }

  private TenantResponse toResponse(TenantEntity entity) {
    int available = entity.getPurchasedLicenses() - entity.getAssignedLicenses();
    return new TenantResponse(
        entity.getId(),
        entity.getName(),
        entity.getPurchasedLicenses(),
        entity.getAssignedLicenses(),
        Math.max(0, available),
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
}
