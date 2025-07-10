package com.flytbase.drone.service;

import com.flytbase.drone.dto.auth.JwtResponse;
import com.flytbase.drone.dto.auth.LoginRequest;
import com.flytbase.drone.dto.auth.RegisterRequest;
import com.flytbase.drone.dto.auth.UserResponse;
import com.flytbase.drone.entity.Organization;
import com.flytbase.drone.entity.User;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.OrganizationRepository;
import com.flytbase.drone.repository.UserRepository;
import com.flytbase.drone.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for authentication operations. */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;

  /**
   * Authenticate a user and generate a JWT token.
   *
   * @param loginRequest the login request
   * @return the JWT response
   */
  public JwtResponse authenticateUser(LoginRequest loginRequest) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = tokenProvider.generateToken(authentication);

    User user =
        userRepository
            .findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new BusinessException("User not found"));

    return new JwtResponse(jwt, user.getId(), user.getEmail(), user.getRole().name());
  }

  /**
   * Register a new user and organization.
   *
   * @param registerRequest the registration request
   * @return the user response
   */
  @Transactional
  public UserResponse registerUser(RegisterRequest registerRequest) {
    // Check if email already exists
    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      throw new BusinessException("Email is already taken");
    }

    // Check if organization name already exists
    if (organizationRepository.existsByName(registerRequest.getOrganizationName())) {
      throw new BusinessException("Organization name is already taken");
    }

    // Create new organization
    Organization organization = new Organization();
    organization.setName(registerRequest.getOrganizationName());
    organization = organizationRepository.save(organization);

    // Create new user
    User user = new User();
    user.setEmail(registerRequest.getEmail());
    user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
    user.setFullName(registerRequest.getFullName());
    user.setRole(registerRequest.getRole());
    user.setOrganization(organization);

    user = userRepository.save(user);

    return UserResponse.fromEntity(user);
  }

  /**
   * Get the current authenticated user.
   *
   * @return the user response
   */
  public UserResponse getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found"));

    return UserResponse.fromEntity(user);
  }
}
