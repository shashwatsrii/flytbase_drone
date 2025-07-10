package com.flytbase.drone.dto.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for login request. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;
}
