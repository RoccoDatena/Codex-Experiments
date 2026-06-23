package com.orbit.licensehub.service;

import com.orbit.licensehub.entity.UserRole;
import com.orbit.licensehub.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccessScopeService {

  public UserPrincipal currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
      throw new AccessDeniedException("Unauthorized");
    }
    return principal;
  }

  public boolean isSuperAdmin() {
    return currentUser().getRole() == UserRole.SUPER_ADMIN;
  }

  public Long enforceTenantScope(Long requestedTenantId) {
    UserPrincipal principal = currentUser();
    if (principal.getRole() == UserRole.SUPER_ADMIN) {
      return requestedTenantId;
    }

    Long currentTenant = principal.getTenantId();
    if (currentTenant == null) {
      throw new AccessDeniedException("Tenant missing on current user");
    }

    if (requestedTenantId != null && !requestedTenantId.equals(currentTenant)) {
      throw new AccessDeniedException("Cross-tenant access denied");
    }

    return currentTenant;
  }

  public void requireAnyRole(UserRole... roles) {
    UserRole current = currentUser().getRole();
    for (UserRole role : roles) {
      if (current == role) {
        return;
      }
    }
    throw new AccessDeniedException("Role not allowed");
  }
}
