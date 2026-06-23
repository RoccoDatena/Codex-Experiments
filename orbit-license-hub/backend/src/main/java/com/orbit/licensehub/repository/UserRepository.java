package com.orbit.licensehub.repository;

import com.orbit.licensehub.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByUsername(String username);

  Page<UserEntity> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
      String usernameSearch, String emailSearch, Pageable pageable);

  Page<UserEntity> findByTenantIdAndUsernameContainingIgnoreCaseOrTenantIdAndEmailContainingIgnoreCase(
      Long tenantId1, String usernameSearch, Long tenantId2, String emailSearch, Pageable pageable);
}
