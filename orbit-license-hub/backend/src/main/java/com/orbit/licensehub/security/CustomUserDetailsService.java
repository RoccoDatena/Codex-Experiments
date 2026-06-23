package com.orbit.licensehub.security;

import com.orbit.licensehub.entity.UserEntity;
import com.orbit.licensehub.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new UserPrincipal(
        user.getId(),
        user.getUsername(),
        user.getPasswordHash(),
        user.getRole(),
        user.getTenant() != null ? user.getTenant().getId() : null,
        user.isEnabled(),
        user.getExtraPermissions());
  }
}
