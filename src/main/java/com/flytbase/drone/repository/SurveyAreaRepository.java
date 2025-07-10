package com.flytbase.drone.repository;

import com.flytbase.drone.entity.SurveyArea;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for SurveyArea entity. */
@Repository
public interface SurveyAreaRepository extends JpaRepository<SurveyArea, UUID> {

  /**
   * Find survey areas by organization ID.
   *
   * @param organizationId the organization ID
   * @return list of survey areas belonging to the organization
   */
  List<SurveyArea> findByOrganizationId(Long organizationId);

  /**
   * Find a survey area by name and organization ID.
   *
   * @param name the name
   * @param organizationId the organization ID
   * @return an Optional containing the survey area if found, or empty if not found
   */
  Optional<SurveyArea> findByNameAndOrganizationId(String name, Long organizationId);

  /**
   * Check if a survey area with the given name exists in the organization.
   *
   * @param name the name
   * @param organizationId the organization ID
   * @return true if a survey area with the name exists in the organization, false otherwise
   */
  boolean existsByNameAndOrganizationId(String name, Long organizationId);
}
