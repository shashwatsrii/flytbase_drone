package com.flytbase.drone.controller;

import com.flytbase.drone.dto.flightpath.FlightPathResponse;
import com.flytbase.drone.dto.mission.CreateMissionRequest;
import com.flytbase.drone.dto.mission.MissionResponse;
import com.flytbase.drone.dto.mission.PatternGenerationRequest;
import com.flytbase.drone.dto.mission.UpdateMissionRequest;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.service.MissionService;
import com.flytbase.drone.service.MissionWebSocketService;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for mission operations. */
@RestController
@RequestMapping("/api/missions")
@CrossOrigin
@RequiredArgsConstructor
public class MissionController {

  private final MissionService missionService;
  private final MissionWebSocketService missionWebSocketService;

  /**
   * Get all missions.
   *
   * @return list of mission responses
   */
  @GetMapping
  public ResponseEntity<List<MissionResponse>> getAllMissions() {
    return ResponseEntity.ok(missionService.getAllMissions());
  }

  /**
   * Get a mission by ID.
   *
   * @param id the mission ID
   * @return the mission response
   */
  @GetMapping("/{id}")
  public ResponseEntity<MissionResponse> getMissionById(@PathVariable UUID id) {
    return ResponseEntity.ok(missionService.getMissionById(id));
  }

  /**
   * Create a new mission.
   *
   * @param request the create mission request
   * @return the created mission response
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionResponse> createMission(
      @Valid @RequestBody CreateMissionRequest request) {
    MissionResponse response = missionService.createMission(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing mission.
   *
   * @param id the mission ID
   * @param request the update mission request
   * @return the updated mission response
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionResponse> updateMission(
      @PathVariable UUID id, @Valid @RequestBody UpdateMissionRequest request) {
    return ResponseEntity.ok(missionService.updateMission(id, request));
  }

  /**
   * Delete a mission.
   *
   * @param id the mission ID
   * @return no content response
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteMission(@PathVariable UUID id) {
    missionService.deleteMission(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get missions by status.
   *
   * @param status the mission status
   * @return list of mission responses
   */
  @GetMapping("/status/{status}")
  public ResponseEntity<List<MissionResponse>> getMissionsByStatus(
      @PathVariable Mission.MissionStatus status) {
    return ResponseEntity.ok(missionService.getMissionsByStatus(status));
  }

  /**
   * Start a mission.
   *
   * @param id the mission ID
   * @return the updated mission response
   */
  @PostMapping("/{id}/start")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionResponse> startMission(@PathVariable UUID id) {
    UpdateMissionRequest request = new UpdateMissionRequest();
    request.setStatus(Mission.MissionStatus.ACTIVE);
    return ResponseEntity.ok(missionService.updateMission(id, request));
  }

  /**
   * Pause a mission.
   *
   * @param id the mission ID
   * @return the updated mission response
   */
  @PostMapping("/{id}/pause")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionResponse> pauseMission(@PathVariable UUID id) {
    UpdateMissionRequest request = new UpdateMissionRequest();
    request.setStatus(Mission.MissionStatus.PAUSED);
    return ResponseEntity.ok(missionService.updateMission(id, request));
  }

  /**
   * Resume a mission.
   *
   * @param id the mission ID
   * @return the updated mission response
   */
  @PostMapping("/{id}/resume")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionResponse> resumeMission(@PathVariable UUID id) {
    UpdateMissionRequest request = new UpdateMissionRequest();
    request.setStatus(Mission.MissionStatus.ACTIVE);
    return ResponseEntity.ok(missionService.updateMission(id, request));
  }

  /**
   * Complete a mission.
   *
   * @param id the mission ID
   * @return the updated mission response
   */
  @PostMapping("/{id}/complete")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionResponse> completeMission(@PathVariable UUID id) {
    UpdateMissionRequest request = new UpdateMissionRequest();
    request.setStatus(Mission.MissionStatus.COMPLETED);
    return ResponseEntity.ok(missionService.updateMission(id, request));
  }

  /**
   * Abort a mission.
   *
   * @param id the mission ID
   * @return the updated mission response
   */
  @PostMapping("/{id}/abort")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<MissionResponse> abortMission(@PathVariable UUID id) {
    UpdateMissionRequest request = new UpdateMissionRequest();
    request.setStatus(Mission.MissionStatus.ABORTED);
    return ResponseEntity.ok(missionService.updateMission(id, request));
  }

  /**
   * Generate a flight path pattern for a mission.
   *
   * @param id the mission ID
   * @param request the pattern generation request
   * @return the flight path response
   */
  @PostMapping("/{id}/generate-pattern")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<FlightPathResponse> generatePattern(
      @PathVariable UUID id, @Valid @RequestBody PatternGenerationRequest request) {
    return ResponseEntity.ok(missionService.generatePattern(id, request));
  }

  /**
   * Simulate drone movement for testing purposes. This endpoint is for development and testing
   * only.
   *
   * @param id the mission ID
   * @param waypointCount the number of waypoints to simulate
   * @return success message
   */
  @PostMapping("/{id}/simulate")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<String> simulateMission(
      @PathVariable UUID id, @RequestParam(defaultValue = "20") int waypointCount) {
    // Verify mission exists
    missionService.getMissionById(id);

    // Start simulation
    missionWebSocketService.simulateDroneMovement(id, waypointCount);

    return ResponseEntity.ok(
        "Simulation started for mission " + id + " with " + waypointCount + " waypoints");
  }
}
