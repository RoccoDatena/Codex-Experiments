package com.orbit.licensehub.service;

import com.orbit.licensehub.dto.PageResponse;
import com.orbit.licensehub.dto.UserRequest;
import com.orbit.licensehub.dto.UserResponse;
import com.orbit.licensehub.entity.TenantEntity;
import com.orbit.licensehub.entity.UserEntity;
import com.orbit.licensehub.entity.UserRole;
import com.orbit.licensehub.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final TenantService tenantService;
  private final AccessScopeService accessScopeService;
  private final PageMapperService pageMapperService;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      TenantService tenantService,
      AccessScopeService accessScopeService,
      PageMapperService pageMapperService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.tenantService = tenantService;
    this.accessScopeService = accessScopeService;
    this.pageMapperService = pageMapperService;
    this.passwordEncoder = passwordEncoder;
  }

  public PageResponse<UserResponse> list(int page, int size, String search, String sort, Long tenantId) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN);

    Long scopedTenantId = accessScopeService.enforceTenantScope(tenantId);
    Pageable pageable = pageRequest(page, size, sort);

    Page<UserEntity> result =
        scopedTenantId == null
            ? userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search, search, pageable)
            : userRepository.findByTenantIdAndUsernameContainingIgnoreCaseOrTenantIdAndEmailContainingIgnoreCase(
                scopedTenantId, search, scopedTenantId, search, pageable);

    return pageMapperService.toResponse(result.map(this::toResponse));
  }

  public UserResponse create(UserRequest request) {
    accessScopeService.requireAnyRole(UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN);

    Long scopedTenantId = accessScopeService.enforceTenantScope(request.tenantId());
    Long targetTenantId = scopedTenantId != null ? scopedTenantId : request.tenantId();

    if (request.role() == UserRole.SUPER_ADMIN && !accessScopeService.isSuperAdmin()) {
      throw new IllegalArgumentException("Only super admin can create super admin users");
    }

    UserEntity entity = new UserEntity();
    entity.setUsername(request.username());
    entity.setEmail(request.email());
    entity.setPasswordHash(passwordEncoder.encode(request.password()));
    entity.setRole(request.role());
    entity.setEnabled(true);

    if (targetTenantId != null) {
      TenantEntity tenant = tenantService.getEntity(targetTenantId);
      entity.setTenant(tenant);
    }

    Set<String> extras = request.extraPermissions() == null ? Set.of() : request.extraPermissions();
    entity.setExtraPermissions(new HashSet<>(extras));

    return toResponse(userRepository.save(entity));
  }

  private UserResponse toResponse(UserEntity entity) {
    return new UserResponse(
        entity.getId(),
        entity.getUsername(),
        entity.getEmail(),
        entity.getRole(),
        entity.getTenant() != null ? entity.getTenant().getId() : null,
        entity.getExtraPermissions(),
        entity.isEnabled());
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
