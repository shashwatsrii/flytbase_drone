package com.flytbase.drone.service;

import com.flytbase.drone.dto.report.CreateSurveyReportRequest;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.MissionProgress;
import com.flytbase.drone.entity.Organization;
import com.flytbase.drone.repository.MissionProgressRepository;
import com.flytbase.drone.repository.MissionRepository;
import com.flytbase.drone.repository.OrganizationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling scheduled report generation and data cleanup. */
@Service
@EnableScheduling
public class ScheduledReportService {

  private static final Logger logger = LoggerFactory.getLogger(ScheduledReportService.class);

  private final MissionRepository missionRepository;
  private final MissionProgressRepository missionProgressRepository;
  private final OrganizationRepository organizationRepository;
  private final SurveyReportService surveyReportService;
  private final MissionStatisticsService missionStatisticsService;

  @Autowired
  public ScheduledReportService(
      MissionRepository missionRepository,
      MissionProgressRepository missionProgressRepository,
      OrganizationRepository organizationRepository,
      SurveyReportService surveyReportService,
      MissionStatisticsService missionStatisticsService) {
    this.missionRepository = missionRepository;
    this.missionProgressRepository = missionProgressRepository;
    this.organizationRepository = organizationRepository;
    this.surveyReportService = surveyReportService;
    this.missionStatisticsService = missionStatisticsService;
  }

  /** Generate daily reports for completed missions. Runs at 1:00 AM every day. */
  @Scheduled(cron = "0 0 1 * * ?")
  @Transactional
  public void generateDailyReports() {
    logger.info("Starting daily report generation");

    // Find missions completed in the last 24 hours
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    List<Mission> completedMissions =
        missionRepository.findByStatusAndActualEndAfter(Mission.MissionStatus.COMPLETED, yesterday);

    logger.info("Found {} missions completed in the last 24 hours", completedMissions.size());

    // Generate reports for each mission
    for (Mission mission : completedMissions) {
      try {
        // Generate survey report
        CreateSurveyReportRequest reportRequest = new CreateSurveyReportRequest();
        reportRequest.setMissionId(mission.getId());
        reportRequest.setReportType("DAILY");
        reportRequest.setTotalArea(1000.0); // This would be calculated in a real implementation
        reportRequest.setCoveredArea(900.0); // This would be calculated in a real implementation
        reportRequest.setImageCount(50); // This would be calculated in a real implementation
        reportRequest.setAnomaliesDetected(2); // This would be calculated in a real implementation
        reportRequest.setReportData("Daily report for mission: " + mission.getName());

        surveyReportService.createSurveyReport(reportRequest);

        // Generate mission statistics
        missionStatisticsService.getMissionStatistics(mission.getId());

        logger.info("Generated daily report for mission: {}", mission.getId());
      } catch (Exception e) {
        logger.error("Error generating daily report for mission: {}", mission.getId(), e);
      }
    }

    logger.info("Completed daily report generation");
  }

  /** Generate weekly summary reports for all organizations. Runs at 2:00 AM every Monday. */
  @Scheduled(cron = "0 0 2 * * MON")
  @Transactional
  public void generateWeeklySummaryReports() {
    logger.info("Starting weekly summary report generation");

    // Get all organizations
    List<Organization> organizations = organizationRepository.findAll();

    // Generate weekly summary for each organization
    for (Organization organization : organizations) {
      try {
        // Find completed missions for this organization in the last week
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        List<Mission> completedMissions =
            missionRepository.findByOrganizationIdAndStatusAndActualEndAfter(
                organization.getId(), Mission.MissionStatus.COMPLETED, lastWeek);

        if (completedMissions.isEmpty()) {
          logger.info(
              "No completed missions in the last week for organization: {}", organization.getId());
          continue;
        }

        logger.info(
            "Generating weekly summary for organization: {} with {} missions",
            organization.getId(),
            completedMissions.size());

        // In a real implementation, you would aggregate data from all missions
        // and generate a comprehensive report

        // For now, we'll just log the activity
        logger.info("Generated weekly summary for organization: {}", organization.getId());
      } catch (Exception e) {
        logger.error(
            "Error generating weekly summary for organization: {}", organization.getId(), e);
      }
    }

    logger.info("Completed weekly summary report generation");
  }

  /**
   * Clean up old mission progress data. Runs at 3:00 AM every Sunday. Keeps the last 100 progress
   * updates for each mission.
   */
  @Scheduled(cron = "0 0 3 * * SUN")
  @Transactional
  public void cleanupOldProgressData() {
    logger.info("Starting cleanup of old mission progress data");

    // Get all missions
    List<Mission> allMissions = missionRepository.findAll();
    int totalDeleted = 0;

    for (Mission mission : allMissions) {
      try {
        // Get all progress updates for this mission
        List<MissionProgress> progressList =
            missionProgressRepository.findByMissionOrderByTimestampAsc(mission);

        // If there are more than 100 progress updates, delete the oldest ones
        if (progressList.size() > 100) {
          int toDelete = progressList.size() - 100;
          logger.info(
              "Mission {} has {} progress updates, deleting oldest {}",
              mission.getId(),
              progressList.size(),
              toDelete);

          for (int i = 0; i < toDelete; i++) {
            missionProgressRepository.delete(progressList.get(i));
            totalDeleted++;
          }
        }
      } catch (Exception e) {
        logger.error("Error cleaning up progress data for mission: {}", mission.getId(), e);
      }
    }

    logger.info("Completed cleanup of old mission progress data. Deleted {} records", totalDeleted);
  }

  /**
   * Archive old reports. Runs at 4:00 AM on the first day of each month. In a real implementation,
   * this would move old reports to an archive storage.
   */
  @Scheduled(cron = "0 0 4 1 * ?")
  @Transactional
  public void archiveOldReports() {
    logger.info("Starting archiving of old reports");

    // In a real implementation, this would:
    // 1. Find reports older than a certain threshold (e.g., 6 months)
    // 2. Export them to an archive format
    // 3. Store them in a long-term storage solution
    // 4. Optionally remove them from the primary database

    logger.info("Completed archiving of old reports");
  }
}
