package com.orbit.licensehub.repository;

import com.orbit.licensehub.entity.ApplicationEntity;
import com.orbit.licensehub.entity.TenantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
  Page<ApplicationEntity> findByNameContainingIgnoreCase(String search, Pageable pageable);

  Page<ApplicationEntity> findByTenantAndNameContainingIgnoreCase(
      TenantEntity tenant, String search, Pageable pageable);
}
