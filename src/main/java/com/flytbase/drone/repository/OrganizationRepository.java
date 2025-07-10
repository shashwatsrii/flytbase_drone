package com.flytbase.drone.repository;

import com.flytbase.drone.entity.Organization;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Organization entity. */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

  /**
   * Find an organization by name.
   *
   * @param name the name to search for
   * @return an Optional containing the organization if found, or empty if not found
   */
  Optional<Organization> findByName(String name);

  /**
   * Check if an organization with the given name exists.
   *
   * @param name the name to check
   * @return true if an organization with the name exists, false otherwise
   */
  boolean existsByName(String name);
}
