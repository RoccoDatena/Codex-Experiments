package com.orbit.licensehub.security;

import com.orbit.licensehub.entity.UserRole;
import java.util.Collection;
import java.util.Set;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserPrincipal implements UserDetails {

  private final Long id;
  private final String username;
  private final String password;
  private final UserRole role;
  private final Long tenantId;
  private final boolean enabled;
  private final Set<String> extraPermissions;

  public UserPrincipal(
      Long id,
      String username,
      String password,
      UserRole role,
      Long tenantId,
      boolean enabled,
      Set<String> extraPermissions) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.role = role;
    this.tenantId = tenantId;
    this.enabled = enabled;
    this.extraPermissions = extraPermissions;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Set.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
