package com.orbit.licensehub.repository;

import com.orbit.licensehub.entity.TenantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {
  Page<TenantEntity> findByNameContainingIgnoreCase(String search, Pageable pageable);
}
