package com.flytbase.drone.service;

import com.flytbase.drone.dto.mission.MissionProgressRequest;
import com.flytbase.drone.dto.mission.MissionProgressResponse;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.MissionProgress;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.MissionProgressRepository;
import com.flytbase.drone.repository.MissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling mission progress operations. */
@Service
public class MissionProgressService {

  private final MissionProgressRepository missionProgressRepository;
  private final MissionRepository missionRepository;

  @Autowired
  public MissionProgressService(
      MissionProgressRepository missionProgressRepository, MissionRepository missionRepository) {
    this.missionProgressRepository = missionProgressRepository;
    this.missionRepository = missionRepository;
  }

  /**
   * Record a new mission progress update.
   *
   * @param request the progress update request
   * @return the created mission progress response
   */
  @Transactional
  public MissionProgressResponse recordProgress(MissionProgressRequest request) {
    // Validate request
    if (request.getMissionId() == null) {
      throw new BusinessException("Mission ID is required");
    }

    // Find mission
    Mission mission =
        missionRepository
            .findById(request.getMissionId())
            .orElseThrow(
                () ->
                    new BusinessException("Mission not found with ID: " + request.getMissionId()));

    // Create and save progress
    MissionProgress progress = new MissionProgress();
    progress.setMission(mission);
    progress.setCurrentWaypointIndex(request.getCurrentWaypointIndex());
    progress.setLatitude(request.getLatitude());
    progress.setLongitude(request.getLongitude());
    progress.setAltitude(request.getAltitude());
    progress.setSpeed(request.getSpeed());
    progress.setBatteryLevel(request.getBatteryLevel());
    progress.setTimestamp(
        request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());

    missionProgressRepository.save(progress);

    // Update mission status if needed
    updateMissionStatus(mission, progress);

    return convertToResponse(progress);
  }

  /**
   * Get the latest progress for a mission.
   *
   * @param missionId the mission ID
   * @return the latest progress response
   */
  public MissionProgressResponse getLatestProgress(UUID missionId) {
    Mission mission =
        missionRepository
            .findById(missionId)
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    MissionProgress progress =
        missionProgressRepository
            .findTopByMissionOrderByTimestampDesc(mission)
            .orElseThrow(
                () -> new BusinessException("No progress found for mission with ID: " + missionId));

    return convertToResponse(progress);
  }

  /**
   * Get all progress updates for a mission.
   *
   * @param missionId the mission ID
   * @return list of progress responses
   */
  public List<MissionProgressResponse> getAllProgressForMission(UUID missionId) {
    List<MissionProgress> progressList =
        missionProgressRepository.findByMissionIdOrderByTimestampAsc(missionId);

    if (progressList.isEmpty()) {
      throw new BusinessException("No progress found for mission with ID: " + missionId);
    }

    return progressList.stream().map(this::convertToResponse).collect(Collectors.toList());
  }

  /**
   * Update mission status based on progress.
   *
   * @param mission the mission
   * @param progress the progress update
   */
  private void updateMissionStatus(Mission mission, MissionProgress progress) {
    // If mission is PLANNED and we get a progress update, set it to ACTIVE
    if (mission.getStatus() == Mission.MissionStatus.PLANNED) {
      mission.setStatus(Mission.MissionStatus.ACTIVE);
      mission.setActualStart(LocalDateTime.now());
      missionRepository.save(mission);
    }
  }

  /**
   * Convert MissionProgress entity to MissionProgressResponse DTO.
   *
   * @param progress the progress entity
   * @return the response DTO
   */
  private MissionProgressResponse convertToResponse(MissionProgress progress) {
    Mission mission = progress.getMission();

    // Calculate completion percentage based on waypoint index
    double completionPercentage = 0.0;
    int totalWaypoints = 0;

    // In a real implementation, you would parse the waypoints from the FlightPath
    // For now, we'll use a placeholder value
    totalWaypoints = 100; // Placeholder

    if (totalWaypoints > 0 && progress.getCurrentWaypointIndex() != null) {
      completionPercentage = (double) progress.getCurrentWaypointIndex() / totalWaypoints * 100;
    }

    return new MissionProgressResponse(
        progress.getId(),
        mission.getId(),
        mission.getName(),
        progress.getCurrentWaypointIndex(),
        totalWaypoints,
        progress.getLatitude(),
        progress.getLongitude(),
        progress.getAltitude(),
        progress.getSpeed(),
        progress.getBatteryLevel(),
        mission.getDrone().getStatus().name(),
        mission.getStatus().name(),
        completionPercentage,
        progress.getTimestamp());
  }
}
