package com.flytbase.drone.repository;

import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.MissionStatistics;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** Repository for managing MissionStatistics entities. */
@Repository
public interface MissionStatisticsRepository extends JpaRepository<MissionStatistics, UUID> {

  /**
   * Find statistics for a specific mission.
   *
   * @param mission the mission
   * @return the mission statistics
   */
  Optional<MissionStatistics> findByMission(Mission mission);

  /**
   * Find statistics for a mission with the given ID.
   *
   * @param missionId the mission ID
   * @return the mission statistics
   */
  Optional<MissionStatistics> findByMissionId(UUID missionId);

  /**
   * Find all statistics for missions in an organization.
   *
   * @param organizationId the organization ID
   * @return list of mission statistics
   */
  List<MissionStatistics> findByMission_Organization_Id(Long organizationId);

  /**
   * Find all statistics for missions with a specific status.
   *
   * @param status the mission status
   * @return list of mission statistics
   */
  List<MissionStatistics> findByMission_Status(Mission.MissionStatus status);

  /**
   * Get average battery usage across all missions.
   *
   * @return the average battery usage
   */
  @Query("SELECT AVG(ms.batteryUsage) FROM MissionStatistics ms")
  Double getAverageBatteryUsage();

  /**
   * Get average completion percentage across all missions.
   *
   * @return the average completion percentage
   */
  @Query("SELECT AVG(ms.completionPercentage) FROM MissionStatistics ms")
  Double getAverageCompletionPercentage();

  /**
   * Get average speed across all missions.
   *
   * @return the average speed
   */
  @Query("SELECT AVG(ms.averageSpeed) FROM MissionStatistics ms")
  Double getAverageSpeed();
}
