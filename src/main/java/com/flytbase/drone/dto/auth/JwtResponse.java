package com.flytbase.drone.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for JWT token response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

  private String token;
  private String tokenType = "Bearer";
  private Long userId;
  private String email;
  private String role;

  public JwtResponse(String token, Long userId, String email, String role) {
    this.token = token;
    this.userId = userId;
    this.email = email;
    this.role = role;
  }
}
