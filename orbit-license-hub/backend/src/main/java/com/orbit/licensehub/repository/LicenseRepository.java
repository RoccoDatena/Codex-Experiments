package com.orbit.licensehub.repository;

import com.orbit.licensehub.entity.LicenseEntity;
import com.orbit.licensehub.entity.TenantEntity;
import com.orbit.licensehub.entity.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseRepository extends JpaRepository<LicenseEntity, Long> {
  Page<LicenseEntity> findByDeviceCodeContainingIgnoreCase(String search, Pageable pageable);

  Page<LicenseEntity> findByTenantAndDeviceCodeContainingIgnoreCase(
      TenantEntity tenant, String search, Pageable pageable);

  long countByApplicationAndStatus(ApplicationEntity application, com.orbit.licensehub.entity.LicenseStatus status);
}
