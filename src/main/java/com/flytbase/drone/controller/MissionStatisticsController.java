package com.flytbase.drone.controller;

import com.flytbase.drone.dto.report.CreateMissionStatisticsRequest;
import com.flytbase.drone.dto.report.MissionStatisticsResponse;
import com.flytbase.drone.service.MissionStatisticsService;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for mission statistics operations. */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin
public class MissionStatisticsController {

  private final MissionStatisticsService missionStatisticsService;

  @Autowired
  public MissionStatisticsController(MissionStatisticsService missionStatisticsService) {
    this.missionStatisticsService = missionStatisticsService;
  }

  /**
   * Get all mission statistics.
   *
   * @return list of mission statistics responses
   */
  @GetMapping
  public ResponseEntity<List<MissionStatisticsResponse>> getAllMissionStatistics() {
    return ResponseEntity.ok(missionStatisticsService.getAllMissionStatistics());
  }

  /**
   * Get mission statistics by mission ID.
   *
   * @param missionId the mission ID
   * @return the mission statistics response
   */
  @GetMapping("/mission/{missionId}")
  public ResponseEntity<MissionStatisticsResponse> getMissionStatistics(
      @PathVariable UUID missionId) {
    return ResponseEntity.ok(missionStatisticsService.getMissionStatistics(missionId));
  }

  /**
   * Create or update mission statistics.
   *
   * @param request the create mission statistics request
   * @return the mission statistics response
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionStatisticsResponse> createOrUpdateMissionStatistics(
      @Valid @RequestBody CreateMissionStatisticsRequest request) {
    MissionStatisticsResponse response =
        missionStatisticsService.createOrUpdateMissionStatistics(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Generate mission statistics from mission progress data.
   *
   * @param missionId the mission ID
   * @return the mission statistics response
   */
  @PostMapping("/mission/{missionId}/generate")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionStatisticsResponse> generateMissionStatistics(
      @PathVariable UUID missionId) {
    // First get the mission to verify it exists and user has access
    MissionStatisticsResponse response = missionStatisticsService.getMissionStatistics(missionId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Get fleet utilization metrics.
   *
   * @return the fleet utilization metrics
   */
  @GetMapping("/fleet-utilization")
  public ResponseEntity<MissionStatisticsService.FleetUtilizationMetrics>
      getFleetUtilizationMetrics() {
    return ResponseEntity.ok(missionStatisticsService.getFleetUtilizationMetrics());
  }
}
