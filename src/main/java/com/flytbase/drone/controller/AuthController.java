package com.flytbase.drone.controller;

import com.flytbase.drone.dto.auth.JwtResponse;
import com.flytbase.drone.dto.auth.LoginRequest;
import com.flytbase.drone.dto.auth.RegisterRequest;
import com.flytbase.drone.dto.auth.UserResponse;
import com.flytbase.drone.service.AuthService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for authentication endpoints. */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * Register a new user.
   *
   * @param registerRequest the registration request
   * @return the user response
   */
  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(
      @Valid @RequestBody RegisterRequest registerRequest) {
    UserResponse userResponse = authService.registerUser(registerRequest);
    return ResponseEntity.ok(userResponse);
  }

  /**
   * Authenticate a user.
   *
   * @param loginRequest the login request
   * @return the JWT response
   */
  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
    return ResponseEntity.ok(jwtResponse);
  }

  /**
   * Get the current authenticated user.
   *
   * @return the user response
   */
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser() {
    UserResponse userResponse = authService.getCurrentUser();
    return ResponseEntity.ok(userResponse);
  }
}
