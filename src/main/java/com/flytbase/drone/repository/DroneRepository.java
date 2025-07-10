package com.flytbase.drone.repository;

import com.flytbase.drone.entity.Drone;
import com.flytbase.drone.entity.Drone.DroneStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Drone entity. */
@Repository
public interface DroneRepository extends JpaRepository<Drone, UUID> {

  /**
   * Find drones by organization ID.
   *
   * @param organizationId the organization ID
   * @return list of drones belonging to the organization
   */
  List<Drone> findByOrganizationId(Long organizationId);

  /**
   * Find drones by organization ID with pagination.
   *
   * @param organizationId the organization ID
   * @param pageable pagination information
   * @return page of drones belonging to the organization
   */
  Page<Drone> findByOrganizationId(Long organizationId, Pageable pageable);

  /**
   * Find a drone by ID and organization ID.
   *
   * @param id the drone ID
   * @param organizationId the organization ID
   * @return an Optional containing the drone if found, or empty if not found
   */
  Optional<Drone> findByIdAndOrganizationId(UUID id, Long organizationId);

  /**
   * Find a drone by serial number.
   *
   * @param serialNumber the serial number
   * @return an Optional containing the drone if found, or empty if not found
   */
  Optional<Drone> findBySerialNumber(String serialNumber);

  /**
   * Find drones by organization ID and status.
   *
   * @param organizationId the organization ID
   * @param status the drone status
   * @return list of drones matching the criteria
   */
  List<Drone> findByOrganizationIdAndStatus(Long organizationId, DroneStatus status);

  /**
   * Check if a drone with the given serial number exists.
   *
   * @param serialNumber the serial number
   * @return true if a drone with the serial number exists, false otherwise
   */
  boolean existsBySerialNumber(String serialNumber);
}
