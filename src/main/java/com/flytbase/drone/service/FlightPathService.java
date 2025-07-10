package com.flytbase.drone.service;

import com.flytbase.drone.dto.flightpath.FlightPathRequest;
import com.flytbase.drone.dto.flightpath.FlightPathResponse;
import com.flytbase.drone.entity.FlightPath;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.SurveyArea;
import com.flytbase.drone.entity.User;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.FlightPathRepository;
import com.flytbase.drone.repository.MissionRepository;
import com.flytbase.drone.util.geometry.GeoJsonParser;
import com.flytbase.drone.util.geometry.WaypointGenerator;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for flight path operations. */
@Service
@RequiredArgsConstructor
@Transactional
public class FlightPathService {

  private final FlightPathRepository flightPathRepository;
  private final MissionRepository missionRepository;
  private final UserService userService;
  private final GeoJsonParser geoJsonParser;
  private final WaypointGenerator waypointGenerator;

  /**
   * Get a flight path by mission ID.
   *
   * @param missionId the mission ID
   * @return the flight path response
   */
  @Transactional(readOnly = true)
  public FlightPathResponse getFlightPathByMissionId(UUID missionId) {
    User currentUser = userService.getCurrentUser();

    // Validate mission
    Mission mission =
        missionRepository
            .findById(missionId)
            .filter(m -> m.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    FlightPath flightPath =
        flightPathRepository
            .findByMissionId(missionId)
            .orElseThrow(
                () -> new BusinessException("Flight path not found for mission ID: " + missionId));

    return FlightPathResponse.fromEntity(flightPath);
  }

  /**
   * Create or update a flight path for a mission.
   *
   * @param missionId the mission ID
   * @param request the flight path request
   * @return the flight path response
   */
  public FlightPathResponse createOrUpdateFlightPath(UUID missionId, FlightPathRequest request) {
    User currentUser = userService.getCurrentUser();

    // Validate mission
    Mission mission =
        missionRepository
            .findById(missionId)
            .filter(m -> m.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    // Only allow flight path creation/update if mission is in PLANNED state
    if (mission.getStatus() != Mission.MissionStatus.PLANNED) {
      throw new BusinessException(
          "Cannot create/update flight path for mission in " + mission.getStatus() + " state");
    }

    // Check if flight path already exists
    FlightPath flightPath =
        flightPathRepository.findByMissionId(missionId).orElse(new FlightPath());

    flightPath.setMission(mission);
    flightPath.setWaypoints(request.getWaypoints());
    flightPath.setTotalDistance(BigDecimal.valueOf(request.getTotalDistance()));
    flightPath.setEstimatedDuration(request.getEstimatedDuration());

    flightPath = flightPathRepository.save(flightPath);
    return FlightPathResponse.fromEntity(flightPath);
  }

  /**
   * Delete a flight path.
   *
   * @param missionId the mission ID
   */
  public void deleteFlightPath(UUID missionId) {
    User currentUser = userService.getCurrentUser();

    // Validate mission
    Mission mission =
        missionRepository
            .findById(missionId)
            .filter(m -> m.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    // Only allow flight path deletion if mission is in PLANNED state
    if (mission.getStatus() != Mission.MissionStatus.PLANNED) {
      throw new BusinessException(
          "Cannot delete flight path for mission in " + mission.getStatus() + " state");
    }

    flightPathRepository.deleteByMissionId(missionId);
  }

  /**
   * Generate a linear pattern flight path.
   *
   * @param surveyArea the survey area
   * @param altitude the flight altitude
   * @param overlapPercentage the overlap percentage
   * @return the generated waypoints as a JSON string
   */
  public String generateLinearPattern(SurveyArea surveyArea, int altitude, int overlapPercentage) {
    try {
      // Parse the boundary polygon from GeoJSON
      Polygon boundary = geoJsonParser.parsePolygon(surveyArea.getBoundaryPolygon());

      // Calculate spacing based on overlap percentage
      // For simplicity, we'll use a fixed base spacing and adjust by overlap
      double baseSpacing = 50.0; // 50 meters between lines
      double spacing = baseSpacing * (1.0 - (overlapPercentage / 100.0));

      // Generate the waypoints
      return waypointGenerator.generateLinearPattern(boundary, altitude, spacing);
    } catch (IOException e) {
      throw new BusinessException("Failed to parse boundary polygon: " + e.getMessage());
    }
  }

  /**
   * Generate a crosshatch pattern flight path.
   *
   * @param surveyArea the survey area
   * @param altitude the flight altitude
   * @param overlapPercentage the overlap percentage
   * @return the generated waypoints as a JSON string
   */
  public String generateCrosshatchPattern(
      SurveyArea surveyArea, int altitude, int overlapPercentage) {
    try {
      // Parse the boundary polygon from GeoJSON
      Polygon boundary = geoJsonParser.parsePolygon(surveyArea.getBoundaryPolygon());

      // Calculate spacing based on overlap percentage
      double baseSpacing = 50.0; // 50 meters between lines
      double spacing = baseSpacing * (1.0 - (overlapPercentage / 100.0));

      // Generate the waypoints
      return waypointGenerator.generateCrosshatchPattern(boundary, altitude, spacing);
    } catch (IOException e) {
      throw new BusinessException("Failed to parse boundary polygon: " + e.getMessage());
    }
  }

  /**
   * Generate a perimeter pattern flight path.
   *
   * @param surveyArea the survey area
   * @param altitude the flight altitude
   * @param overlapPercentage the overlap percentage
   * @return the generated waypoints as a JSON string
   */
  public String generatePerimeterPattern(
      SurveyArea surveyArea, int altitude, int overlapPercentage) {
    try {
      // Parse the boundary polygon from GeoJSON
      Polygon boundary = geoJsonParser.parsePolygon(surveyArea.getBoundaryPolygon());

      // For perimeter pattern, we'll use the overlap percentage to determine
      // the number of concentric rings (1-3 rings based on percentage)
      int numRings =
          1 + (overlapPercentage / 50); // 0-49% = 1 ring, 50-99% = 2 rings, 100% = 3 rings
      numRings = Math.min(numRings, 3); // Cap at 3 rings

      // Generate the waypoints
      return waypointGenerator.generatePerimeterPattern(boundary, altitude, numRings);
    } catch (IOException e) {
      throw new BusinessException("Failed to parse boundary polygon: " + e.getMessage());
    }
  }
}
