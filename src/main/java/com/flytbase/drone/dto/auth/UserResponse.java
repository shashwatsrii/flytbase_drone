package com.flytbase.drone.dto.auth;

import com.flytbase.drone.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for user response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String email;
  private String fullName;
  private String role;
  private Long organizationId;
  private String organizationName;

  /**
   * Create a UserResponse from a User entity.
   *
   * @param user the user entity
   * @return the user response DTO
   */
  public static UserResponse fromEntity(User user) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setEmail(user.getEmail());
    response.setFullName(user.getFullName());
    response.setRole(user.getRole().name());
    response.setOrganizationId(user.getOrganization().getId());
    response.setOrganizationName(user.getOrganization().getName());
    return response;
  }
}
