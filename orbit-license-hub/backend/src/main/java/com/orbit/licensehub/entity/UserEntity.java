package com.orbit.licensehub.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  private TenantEntity tenant;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_extra_permissions", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "permission")
  private Set<String> extraPermissions = new HashSet<>();

  @Column(nullable = false)
  private boolean enabled;
}
