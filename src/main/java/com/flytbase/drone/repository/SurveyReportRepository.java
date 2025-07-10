package com.flytbase.drone.repository;

import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.SurveyReport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for managing SurveyReport entities. */
@Repository
public interface SurveyReportRepository extends JpaRepository<SurveyReport, UUID> {

  /**
   * Find all reports for a specific mission.
   *
   * @param mission the mission
   * @return list of reports
   */
  List<SurveyReport> findByMissionOrderByGeneratedAtDesc(Mission mission);

  /**
   * Find all reports for a mission with the given ID.
   *
   * @param missionId the mission ID
   * @return list of reports
   */
  List<SurveyReport> findByMissionIdOrderByGeneratedAtDesc(UUID missionId);

  /**
   * Find all reports of a specific type.
   *
   * @param reportType the report type
   * @return list of reports
   */
  List<SurveyReport> findByReportTypeOrderByGeneratedAtDesc(String reportType);

  /**
   * Find all reports generated within a date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @return list of reports
   */
  List<SurveyReport> findByGeneratedAtBetweenOrderByGeneratedAtDesc(
      LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Find all reports for missions in an organization.
   *
   * @param organizationId the organization ID
   * @return list of reports
   */
  List<SurveyReport> findByMission_Organization_IdOrderByGeneratedAtDesc(Long organizationId);
}
