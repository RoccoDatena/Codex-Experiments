package com.orbit.licensehub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "client_applications")
@Getter
@Setter
public class ApplicationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @Column(nullable = false)
  private String name;

  @Column(name = "logo_url")
  private String logoUrl;

  @Column(name = "base_color", nullable = false)
  private String baseColor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false)
  private ApplicationEnvironment environment;

  @Column(name = "p12_file_ref")
  private String p12FileRef;

  @Column(name = "p12_password_encrypted", nullable = false)
  private String p12PasswordEncrypted;

  @Column(name = "retention_minutes", nullable = false)
  private Integer retentionMinutes;

  @Column(name = "public_key_pem", nullable = false, columnDefinition = "LONGTEXT")
  private String publicKeyPem;

  @Column(name = "allocated_licenses", nullable = false)
  private Integer allocatedLicenses;

  @Column(name = "client_api_key", nullable = false, unique = true)
  private String clientApiKey;

  @Column(name = "client_secret_encrypted", nullable = false)
  private String clientSecretEncrypted;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationStatus status;
}
