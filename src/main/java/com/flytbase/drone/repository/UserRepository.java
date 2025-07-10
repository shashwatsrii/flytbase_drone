package com.flytbase.drone.repository;

import com.flytbase.drone.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for User entity. */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Find a user by email.
   *
   * @param email the email to search for
   * @return an Optional containing the user if found, or empty if not found
   */
  Optional<User> findByEmail(String email);

  /**
   * Check if a user with the given email exists.
   *
   * @param email the email to check
   * @return true if a user with the email exists, false otherwise
   */
  boolean existsByEmail(String email);
}
