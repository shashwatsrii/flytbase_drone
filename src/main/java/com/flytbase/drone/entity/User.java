package com.flytbase.drone.entity;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing a user in the system. */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  /** Enum representing user roles in the system. */
  public enum Role {
    ADMIN,
    OPERATOR,
    VIEWER
  }
}
