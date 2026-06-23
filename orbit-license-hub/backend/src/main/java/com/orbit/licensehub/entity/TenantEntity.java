package com.orbit.licensehub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tenants")
@Getter
@Setter
public class TenantEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "purchased_licenses", nullable = false)
  private Integer purchasedLicenses;

  @Column(name = "assigned_licenses", nullable = false)
  private Integer assignedLicenses;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TenantStatus status;
}
