package com.flytbase.drone.repository;

import com.flytbase.drone.entity.FlightPath;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for FlightPath entity. */
@Repository
public interface FlightPathRepository extends JpaRepository<FlightPath, UUID> {

  /**
   * Find flight path by mission ID.
   *
   * @param missionId the mission ID
   * @return an Optional containing the flight path if found, or empty if not found
   */
  Optional<FlightPath> findByMissionId(UUID missionId);

  /**
   * Delete flight path by mission ID.
   *
   * @param missionId the mission ID
   */
  void deleteByMissionId(UUID missionId);

  /**
   * Check if a flight path exists for the mission.
   *
   * @param missionId the mission ID
   * @return true if a flight path exists for the mission, false otherwise
   */
  boolean existsByMissionId(UUID missionId);
}
