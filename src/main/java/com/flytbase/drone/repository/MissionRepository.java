package com.flytbase.drone.repository;

import com.flytbase.drone.entity.Mission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Mission entity. */
@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {

  /**
   * Find missions by organization ID.
   *
   * @param organizationId the organization ID
   * @return list of missions belonging to the organization
   */
  List<Mission> findByOrganizationId(Long organizationId);

  /**
   * Find missions by drone ID.
   *
   * @param droneId the drone ID
   * @return list of missions assigned to the drone
   */
  List<Mission> findByDroneId(UUID droneId);

  /**
   * Find missions by survey area ID.
   *
   * @param surveyAreaId the survey area ID
   * @return list of missions for the survey area
   */
  List<Mission> findBySurveyAreaId(UUID surveyAreaId);

  /**
   * Find missions by status.
   *
   * @param status the mission status
   * @return list of missions with the specified status
   */
  List<Mission> findByStatus(Mission.MissionStatus status);

  /**
   * Find missions by organization ID and status.
   *
   * @param organizationId the organization ID
   * @param status the mission status
   * @return list of missions belonging to the organization with the specified status
   */
  List<Mission> findByOrganizationIdAndStatus(Long organizationId, Mission.MissionStatus status);

  /**
   * Find missions by status and actual end date after the specified date.
   *
   * @param status the mission status
   * @param actualEnd the date after which the mission ended
   * @return list of missions with the specified status that ended after the specified date
   */
  List<Mission> findByStatusAndActualEndAfter(
      Mission.MissionStatus status, java.time.LocalDateTime actualEnd);

  /**
   * Find missions by organization ID, status, and actual end date after the specified date.
   *
   * @param organizationId the organization ID
   * @param status the mission status
   * @param actualEnd the date after which the mission ended
   * @return list of missions belonging to the organization with the specified status that ended
   *     after the specified date
   */
  List<Mission> findByOrganizationIdAndStatusAndActualEndAfter(
      Long organizationId, Mission.MissionStatus status, java.time.LocalDateTime actualEnd);

  /**
   * Find top 10 most recent missions by organization ID.
   *
   * @param organizationId the organization ID
   * @return list of 10 most recent missions
   */
  List<Mission> findTop10ByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}
