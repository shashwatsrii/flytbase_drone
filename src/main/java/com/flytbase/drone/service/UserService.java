package com.flytbase.drone.service;

import com.flytbase.drone.entity.User;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for user operations. */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  /**
   * Get the current authenticated user.
   *
   * @return the current user
   */
  @Transactional(readOnly = true)
  public User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new BusinessException("User not found"));
  }
}
