package com.flytbase.drone.service;

import com.flytbase.drone.dto.report.CreateMissionStatisticsRequest;
import com.flytbase.drone.dto.report.MissionStatisticsResponse;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.MissionProgress;
import com.flytbase.drone.entity.MissionStatistics;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.MissionProgressRepository;
import com.flytbase.drone.repository.MissionRepository;
import com.flytbase.drone.repository.MissionStatisticsRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling mission statistics operations. */
@Service
public class MissionStatisticsService {

  private final MissionStatisticsRepository missionStatisticsRepository;
  private final MissionRepository missionRepository;
  private final MissionProgressRepository missionProgressRepository;
  private final UserService userService;

  @Autowired
  public MissionStatisticsService(
      MissionStatisticsRepository missionStatisticsRepository,
      MissionRepository missionRepository,
      MissionProgressRepository missionProgressRepository,
      UserService userService) {
    this.missionStatisticsRepository = missionStatisticsRepository;
    this.missionRepository = missionRepository;
    this.missionProgressRepository = missionProgressRepository;
    this.userService = userService;
  }

  /**
   * Create or update mission statistics.
   *
   * @param request the create mission statistics request
   * @return the mission statistics response
   */
  @Transactional
  public MissionStatisticsResponse createOrUpdateMissionStatistics(
      CreateMissionStatisticsRequest request) {
    // Validate mission
    Mission mission =
        missionRepository
            .findById(request.getMissionId())
            .orElseThrow(
                () ->
                    new BusinessException("Mission not found with ID: " + request.getMissionId()));

    // Verify user has access to the mission's organization
    if (!userService
        .getCurrentUser()
        .getOrganization()
        .getId()
        .equals(mission.getOrganization().getId())) {
      throw new BusinessException("User does not have access to this mission");
    }

    // Check if statistics already exist for this mission
    MissionStatistics statistics =
        missionStatisticsRepository
            .findByMissionId(request.getMissionId())
            .orElse(new MissionStatistics());

    // Update statistics
    statistics.setMission(mission);
    statistics.setTotalDistance(request.getTotalDistance());
    statistics.setActualDuration(request.getActualDuration());
    statistics.setAverageSpeed(request.getAverageSpeed());
    statistics.setMaxAltitude(request.getMaxAltitude());
    statistics.setBatteryUsage(request.getBatteryUsage());
    statistics.setWaypointsCompleted(request.getWaypointsCompleted());
    statistics.setTotalWaypoints(request.getTotalWaypoints());

    // Calculate completion percentage if not provided
    if (request.getCompletionPercentage() == null
        && request.getTotalWaypoints() != null
        && request.getWaypointsCompleted() != null) {
      BigDecimal completionPercentage = BigDecimal.ZERO;
      if (request.getTotalWaypoints() > 0) {
        completionPercentage =
            BigDecimal.valueOf(request.getWaypointsCompleted())
                .divide(BigDecimal.valueOf(request.getTotalWaypoints()), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
      }
      statistics.setCompletionPercentage(completionPercentage);
    } else {
      statistics.setCompletionPercentage(request.getCompletionPercentage());
    }

    statistics = missionStatisticsRepository.save(statistics);
    return MissionStatisticsResponse.fromEntity(statistics);
  }

  /**
   * Get mission statistics by mission ID.
   *
   * @param missionId the mission ID
   * @return the mission statistics response
   */
  public MissionStatisticsResponse getMissionStatistics(UUID missionId) {
    // Verify mission exists and user has access
    Mission mission =
        missionRepository
            .findById(missionId)
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    if (!userService
        .getCurrentUser()
        .getOrganization()
        .getId()
        .equals(mission.getOrganization().getId())) {
      throw new BusinessException("User does not have access to this mission");
    }

    // Get statistics or generate them if they don't exist
    MissionStatistics statistics =
        missionStatisticsRepository
            .findByMissionId(missionId)
            .orElseGet(() -> generateMissionStatistics(mission));

    return MissionStatisticsResponse.fromEntity(statistics);
  }

  /**
   * Get all mission statistics for the current user's organization.
   *
   * @return list of mission statistics responses
   */
  public List<MissionStatisticsResponse> getAllMissionStatistics() {
    Long organizationId = userService.getCurrentUser().getOrganization().getId();
    return missionStatisticsRepository.findByMission_Organization_Id(organizationId).stream()
        .map(MissionStatisticsResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Generate mission statistics from mission progress data.
   *
   * @param mission the mission
   * @return the generated mission statistics
   */
  @Transactional
  public MissionStatistics generateMissionStatistics(Mission mission) {
    // Get all progress updates for the mission
    List<MissionProgress> progressList =
        missionProgressRepository.findByMissionOrderByTimestampAsc(mission);

    // Create new statistics object
    MissionStatistics statistics = new MissionStatistics();
    statistics.setMission(mission);

    // Set default values
    statistics.setTotalDistance(BigDecimal.ZERO);
    statistics.setAverageSpeed(BigDecimal.ZERO);
    statistics.setMaxAltitude(0);
    statistics.setBatteryUsage(BigDecimal.ZERO);
    statistics.setWaypointsCompleted(0);
    statistics.setTotalWaypoints(100); // Default value
    statistics.setCompletionPercentage(BigDecimal.ZERO);

    // If there are progress updates, calculate statistics
    if (!progressList.isEmpty()) {
      // Calculate total distance (simplified)
      double totalDistance = 0.0;
      MissionProgress prevProgress = null;
      int maxAltitude = 0;
      double totalSpeed = 0.0;
      int batteryStart = 100;
      int batteryEnd = 100;

      for (int i = 0; i < progressList.size(); i++) {
        MissionProgress progress = progressList.get(i);

        // Update max altitude
        if (progress.getAltitude() > maxAltitude) {
          maxAltitude = progress.getAltitude();
        }

        // Add to total speed for average calculation
        totalSpeed += progress.getSpeed();

        // Set battery levels
        if (i == 0) {
          batteryStart = progress.getBatteryLevel();
        }
        if (i == progressList.size() - 1) {
          batteryEnd = progress.getBatteryLevel();
        }

        // Calculate distance between points
        if (prevProgress != null) {
          double distance =
              calculateDistance(
                  prevProgress.getLatitude(), prevProgress.getLongitude(),
                  progress.getLatitude(), progress.getLongitude());
          totalDistance += distance;
        }

        prevProgress = progress;
      }

      // Set calculated values
      statistics.setTotalDistance(
          BigDecimal.valueOf(totalDistance).setScale(2, RoundingMode.HALF_UP));
      statistics.setMaxAltitude(maxAltitude);

      // Calculate average speed
      if (progressList.size() > 0) {
        double avgSpeed = totalSpeed / progressList.size();
        statistics.setAverageSpeed(BigDecimal.valueOf(avgSpeed).setScale(2, RoundingMode.HALF_UP));
      }

      // Calculate battery usage
      int batteryUsage = batteryStart - batteryEnd;
      statistics.setBatteryUsage(
          BigDecimal.valueOf(batteryUsage).setScale(2, RoundingMode.HALF_UP));

      // Set waypoints completed
      MissionProgress lastProgress = progressList.get(progressList.size() - 1);
      statistics.setWaypointsCompleted(lastProgress.getCurrentWaypointIndex() + 1);

      // Calculate completion percentage
      if (statistics.getTotalWaypoints() > 0) {
        BigDecimal completionPercentage =
            BigDecimal.valueOf(statistics.getWaypointsCompleted())
                .divide(BigDecimal.valueOf(statistics.getTotalWaypoints()), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        statistics.setCompletionPercentage(completionPercentage);
      }

      // Calculate actual duration if mission has start and end times
      if (mission.getActualStart() != null) {
        LocalDateTime endTime =
            mission.getActualEnd() != null ? mission.getActualEnd() : LocalDateTime.now();
        Duration duration = Duration.between(mission.getActualStart(), endTime);
        statistics.setActualDuration((int) duration.toMinutes());
      }
    }

    // Save and return statistics
    return missionStatisticsRepository.save(statistics);
  }

  /**
   * Calculate distance between two points using Haversine formula.
   *
   * @param lat1 latitude of point 1
   * @param lon1 longitude of point 1
   * @param lat2 latitude of point 2
   * @param lon2 longitude of point 2
   * @return distance in meters
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371; // Radius of the earth in km

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters

    return distance;
  }

  /**
   * Get fleet utilization metrics.
   *
   * @return the fleet utilization metrics
   */
  public FleetUtilizationMetrics getFleetUtilizationMetrics() {
    // This would be implemented with more complex queries in a real application
    // For now, we'll return placeholder data

    Double avgBatteryUsage = missionStatisticsRepository.getAverageBatteryUsage();
    Double avgCompletionPercentage = missionStatisticsRepository.getAverageCompletionPercentage();
    Double avgSpeed = missionStatisticsRepository.getAverageSpeed();

    return new FleetUtilizationMetrics(
        avgBatteryUsage != null ? avgBatteryUsage : 0.0,
        avgCompletionPercentage != null ? avgCompletionPercentage : 0.0,
        avgSpeed != null ? avgSpeed : 0.0);
  }

  /** Inner class representing fleet utilization metrics. */
  public static class FleetUtilizationMetrics {
    private final double averageBatteryUsage;
    private final double averageCompletionRate;
    private final double averageSpeed;

    public FleetUtilizationMetrics(
        double averageBatteryUsage, double averageCompletionRate, double averageSpeed) {
      this.averageBatteryUsage = averageBatteryUsage;
      this.averageCompletionRate = averageCompletionRate;
      this.averageSpeed = averageSpeed;
    }

    public double getAverageBatteryUsage() {
      return averageBatteryUsage;
    }

    public double getAverageCompletionRate() {
      return averageCompletionRate;
    }

    public double getAverageSpeed() {
      return averageSpeed;
    }
  }
}
