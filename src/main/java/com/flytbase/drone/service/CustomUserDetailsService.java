package com.flytbase.drone.service;

import com.flytbase.drone.entity.User;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service to load user-specific data for authentication. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found with email: " + email));

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPasswordHash(),
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
  }
}
