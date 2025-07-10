package com.flytbase.drone.controller;

import com.flytbase.drone.dto.flightpath.FlightPathRequest;
import com.flytbase.drone.dto.flightpath.FlightPathResponse;
import com.flytbase.drone.dto.mission.MissionResponse;
import com.flytbase.drone.dto.surveyarea.SurveyAreaResponse;
import com.flytbase.drone.entity.Mission.PatternType;
import com.flytbase.drone.service.FlightPathService;
import com.flytbase.drone.service.MissionService;
import com.flytbase.drone.service.SurveyAreaService;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for flight path operations. */
@RestController
@RequestMapping("/api/flight-paths")
@CrossOrigin
@RequiredArgsConstructor
public class FlightPathController {

  private final FlightPathService flightPathService;
  private final MissionService missionService;
  private final SurveyAreaService surveyAreaService;

  /**
   * Get a flight path by mission ID.
   *
   * @param missionId the mission ID
   * @return the flight path response
   */
  @GetMapping("/mission/{missionId}")
  public ResponseEntity<FlightPathResponse> getFlightPathByMissionId(@PathVariable UUID missionId) {
    return ResponseEntity.ok(flightPathService.getFlightPathByMissionId(missionId));
  }

  /**
   * Create or update a flight path for a mission.
   *
   * @param missionId the mission ID
   * @param request the flight path request
   * @return the flight path response
   */
  @PostMapping("/mission/{missionId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<FlightPathResponse> createOrUpdateFlightPath(
      @PathVariable UUID missionId, @Valid @RequestBody FlightPathRequest request) {
    FlightPathResponse response = flightPathService.createOrUpdateFlightPath(missionId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Delete a flight path.
   *
   * @param missionId the mission ID
   * @return no content response
   */
  @DeleteMapping("/mission/{missionId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteFlightPath(@PathVariable UUID missionId) {
    flightPathService.deleteFlightPath(missionId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Generate a flight path for a mission based on pattern type.
   *
   * @param missionId the mission ID
   * @return the flight path response
   */
  @PostMapping("/mission/{missionId}/generate")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<FlightPathResponse> generateFlightPath(@PathVariable UUID missionId) {
    // Get mission details
    MissionResponse missionResponse = missionService.getMissionById(missionId);
    SurveyAreaResponse surveyAreaResponse =
        surveyAreaService.getSurveyAreaById(missionResponse.getSurveyAreaId());

    // Generate waypoints based on pattern type
    String waypoints;
    PatternType patternType = PatternType.valueOf(missionResponse.getPatternType());

    switch (patternType) {
      case LINEAR:
        waypoints =
            flightPathService.generateLinearPattern(
                null, missionResponse.getFlightAltitude(), missionResponse.getOverlapPercentage());
        break;
      case CROSSHATCH:
        waypoints =
            flightPathService.generateCrosshatchPattern(
                null, missionResponse.getFlightAltitude(), missionResponse.getOverlapPercentage());
        break;
      case PERIMETER:
        waypoints =
            flightPathService.generatePerimeterPattern(
                null, missionResponse.getFlightAltitude(), missionResponse.getOverlapPercentage());
        break;
      default:
        waypoints =
            flightPathService.generateLinearPattern(
                null, missionResponse.getFlightAltitude(), missionResponse.getOverlapPercentage());
    }

    // Create flight path request
    FlightPathRequest request = new FlightPathRequest();
    request.setPatternType(patternType);
    request.setWaypoints(waypoints);

    // Calculate approximate distance and duration (simplified)
    // In a real application, these would be calculated based on the waypoints
    request.setTotalDistance(500.0); // 500 meters
    request.setEstimatedDuration(300); // 5 minutes

    return ResponseEntity.ok(flightPathService.createOrUpdateFlightPath(missionId, request));
  }
}
