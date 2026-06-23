package com.orbit.licensehub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "licenses")
@Getter
@Setter
public class LicenseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id")
  private ApplicationEntity application;

  @Column(name = "device_code", nullable = false)
  private String deviceCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LicenseStatus status;

  @Column(name = "last_used_at")
  private Instant lastUsedAt;
}
