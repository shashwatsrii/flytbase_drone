package com.flytbase.drone.dto.auth;

import com.flytbase.drone.entity.User;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for user registration request. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  @NotBlank(message = "Organization name is required")
  private String organizationName;

  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;

  private User.Role role = User.Role.ADMIN; // Default role for registration is ADMIN
}
