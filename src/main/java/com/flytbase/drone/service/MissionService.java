package com.flytbase.drone.service;

import com.flytbase.drone.dto.flightpath.FlightPathRequest;
import com.flytbase.drone.dto.flightpath.FlightPathResponse;
import com.flytbase.drone.dto.mission.CreateMissionRequest;
import com.flytbase.drone.dto.mission.MissionResponse;
import com.flytbase.drone.dto.mission.PatternGenerationRequest;
import com.flytbase.drone.dto.mission.UpdateMissionRequest;
import com.flytbase.drone.entity.Drone;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.SurveyArea;
import com.flytbase.drone.entity.User;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.DroneRepository;
import com.flytbase.drone.repository.MissionRepository;
import com.flytbase.drone.repository.SurveyAreaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for mission operations. */
@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

  private final MissionRepository missionRepository;
  private final DroneRepository droneRepository;
  private final SurveyAreaRepository surveyAreaRepository;
  private final UserService userService;
  private final FlightPathService flightPathService;
  private final MissionWebSocketService webSocketService;

  /**
   * Get all missions for the current user's organization.
   *
   * @return list of mission responses
   */
  @Transactional(readOnly = true)
  public List<MissionResponse> getAllMissions() {
    User currentUser = userService.getCurrentUser();
    return missionRepository.findByOrganizationId(currentUser.getOrganization().getId()).stream()
        .map(MissionResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get a mission by ID.
   *
   * @param id the mission ID
   * @return the mission response
   */
  @Transactional(readOnly = true)
  public MissionResponse getMissionById(UUID id) {
    User currentUser = userService.getCurrentUser();
    Mission mission =
        missionRepository
            .findById(id)
            .filter(m -> m.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + id));
    return MissionResponse.fromEntity(mission);
  }

  /**
   * Create a new mission.
   *
   * @param request the create mission request
   * @return the created mission response
   */
  public MissionResponse createMission(CreateMissionRequest request) {
    User currentUser = userService.getCurrentUser();

    // Validate drone
    Drone drone =
        droneRepository
            .findByIdAndOrganizationId(request.getDroneId(), currentUser.getOrganization().getId())
            .orElseThrow(
                () -> new BusinessException("Drone not found with ID: " + request.getDroneId()));

    // Check if drone is available
    if (drone.getStatus() != Drone.DroneStatus.AVAILABLE) {
      throw new BusinessException("Drone is not available for mission");
    }

    // Validate survey area
    SurveyArea surveyArea =
        surveyAreaRepository
            .findById(request.getSurveyAreaId())
            .filter(
                sa -> sa.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Survey area not found with ID: " + request.getSurveyAreaId()));

    Mission mission = new Mission();
    mission.setName(request.getName());
    mission.setDescription(request.getDescription());
    mission.setDrone(drone);
    mission.setSurveyArea(surveyArea);
    mission.setCreatedBy(currentUser);
    mission.setOrganization(currentUser.getOrganization());
    mission.setType(request.getType());
    mission.setStatus(Mission.MissionStatus.PLANNED);
    mission.setScheduledStart(request.getScheduledStart());
    mission.setFlightAltitude(request.getFlightAltitude());
    mission.setSpeed(request.getSpeed());
    mission.setOverlapPercentage(request.getOverlapPercentage());
    mission.setPatternType(request.getPatternType());

    // Update drone status
    drone.setStatus(Drone.DroneStatus.IN_MISSION);
    droneRepository.save(drone);

    mission = missionRepository.save(mission);
    return MissionResponse.fromEntity(mission);
  }

  /**
   * Update an existing mission.
   *
   * @param id the mission ID
   * @param request the update mission request
   * @return the updated mission response
   */
  public MissionResponse updateMission(UUID id, UpdateMissionRequest request) {
    User currentUser = userService.getCurrentUser();
    Mission mission =
        missionRepository
            .findById(id)
            .filter(m -> m.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + id));

    // If this is only a status change, allow it based on valid transitions
    boolean isOnlyStatusChange =
        request.getStatus() != null
            && request.getName() == null
            && request.getDescription() == null
            && request.getDroneId() == null
            && request.getSurveyAreaId() == null
            && request.getType() == null
            && request.getScheduledStart() == null
            && request.getFlightAltitude() == null
            && request.getSpeed() == null
            && request.getOverlapPercentage() == null
            && request.getPatternType() == null;

    // For non-status changes, only allow updates if mission is in PLANNED or PAUSED state
    if (!isOnlyStatusChange
        && mission.getStatus() != Mission.MissionStatus.PLANNED
        && mission.getStatus() != Mission.MissionStatus.PAUSED) {
      throw new BusinessException("Cannot update mission in " + mission.getStatus() + " state");
    }

    if (request.getName() != null) {
      mission.setName(request.getName());
    }
    if (request.getDescription() != null) {
      mission.setDescription(request.getDescription());
    }
    if (request.getDroneId() != null && !request.getDroneId().equals(mission.getDrone().getId())) {
      // Validate new drone
      Drone newDrone =
          droneRepository
              .findByIdAndOrganizationId(
                  request.getDroneId(), currentUser.getOrganization().getId())
              .orElseThrow(
                  () -> new BusinessException("Drone not found with ID: " + request.getDroneId()));

      // Check if new drone is available
      if (newDrone.getStatus() != Drone.DroneStatus.AVAILABLE) {
        throw new BusinessException("New drone is not available for mission");
      }

      // Release old drone
      Drone oldDrone = mission.getDrone();
      oldDrone.setStatus(Drone.DroneStatus.AVAILABLE);
      droneRepository.save(oldDrone);

      // Assign new drone
      newDrone.setStatus(Drone.DroneStatus.IN_MISSION);
      droneRepository.save(newDrone);
      mission.setDrone(newDrone);
    }
    if (request.getSurveyAreaId() != null
        && !request.getSurveyAreaId().equals(mission.getSurveyArea().getId())) {
      // Validate new survey area
      SurveyArea newSurveyArea =
          surveyAreaRepository
              .findById(request.getSurveyAreaId())
              .filter(
                  sa -> sa.getOrganization().getId().equals(currentUser.getOrganization().getId()))
              .orElseThrow(
                  () ->
                      new BusinessException(
                          "Survey area not found with ID: " + request.getSurveyAreaId()));
      mission.setSurveyArea(newSurveyArea);
    }
    if (request.getType() != null) {
      mission.setType(request.getType());
    }
    if (request.getStatus() != null) {
      // Only allow certain status transitions
      Mission.MissionStatus oldStatus = mission.getStatus();
      Mission.MissionStatus newStatus = request.getStatus();

      // Validate status transitions
      boolean validTransition = false;
      String transitionError = "";

      switch (oldStatus) {
        case PLANNED:
          validTransition =
              newStatus == Mission.MissionStatus.ACTIVE
                  || newStatus == Mission.MissionStatus.ABORTED;
          transitionError = "PLANNED missions can only be started (ACTIVE) or aborted";
          break;
        case ACTIVE:
          validTransition =
              newStatus == Mission.MissionStatus.PAUSED
                  || newStatus == Mission.MissionStatus.COMPLETED
                  || newStatus == Mission.MissionStatus.ABORTED;
          transitionError = "ACTIVE missions can only be paused, completed, or aborted";
          break;
        case PAUSED:
          validTransition =
              newStatus == Mission.MissionStatus.ACTIVE
                  || newStatus == Mission.MissionStatus.ABORTED;
          transitionError = "PAUSED missions can only be resumed (ACTIVE) or aborted";
          break;
        case COMPLETED:
        case ABORTED:
          validTransition = false;
          transitionError = oldStatus + " missions cannot be changed";
          break;
      }

      if (!validTransition) {
        throw new BusinessException(
            "Invalid status transition from "
                + oldStatus
                + " to "
                + newStatus
                + ". "
                + transitionError);
      }

      // Handle status-specific logic
      if (newStatus == Mission.MissionStatus.ACTIVE && oldStatus == Mission.MissionStatus.PLANNED) {
        mission.setActualStart(LocalDateTime.now());

        // Notify subscribers that mission is now active
        webSocketService.sendStatusChangeNotification(
            mission.getId(), newStatus.name(), "Mission has been activated");

      } else if (newStatus == Mission.MissionStatus.PAUSED
          && oldStatus == Mission.MissionStatus.ACTIVE) {

        // Notify subscribers that mission is paused
        webSocketService.sendStatusChangeNotification(
            mission.getId(), newStatus.name(), "Mission has been paused");

      } else if (newStatus == Mission.MissionStatus.ACTIVE
          && oldStatus == Mission.MissionStatus.PAUSED) {
        // Resuming from pause
        webSocketService.sendStatusChangeNotification(
            mission.getId(), newStatus.name(), "Mission has been resumed");

      } else if (newStatus == Mission.MissionStatus.COMPLETED
          || newStatus == Mission.MissionStatus.ABORTED) {
        mission.setActualEnd(LocalDateTime.now());

        // Release drone
        Drone drone = mission.getDrone();
        drone.setStatus(Drone.DroneStatus.AVAILABLE);

        // If mission completed successfully, update drone flight hours
        if (newStatus == Mission.MissionStatus.COMPLETED && mission.getActualStart() != null) {
          // Calculate flight duration in hours
          long flightMinutes =
              java.time.Duration.between(mission.getActualStart(), mission.getActualEnd())
                  .toMinutes();
          double flightHours = flightMinutes / 60.0;

          // Update drone's total flight hours
          BigDecimal currentHours =
              drone.getTotalFlightHours() != null ? drone.getTotalFlightHours() : BigDecimal.ZERO;
          drone.setTotalFlightHours(currentHours.add(BigDecimal.valueOf(flightHours)));
        }

        droneRepository.save(drone);

        // Notify subscribers that mission is completed or aborted
        String message =
            newStatus == Mission.MissionStatus.COMPLETED
                ? "Mission has been completed successfully"
                : "Mission has been aborted";

        webSocketService.sendStatusChangeNotification(mission.getId(), newStatus.name(), message);
      }

      mission.setStatus(newStatus);
    }
    if (request.getScheduledStart() != null) {
      mission.setScheduledStart(request.getScheduledStart());
    }
    if (request.getFlightAltitude() != null) {
      mission.setFlightAltitude(request.getFlightAltitude());
    }
    if (request.getSpeed() != null) {
      mission.setSpeed(request.getSpeed());
    }
    if (request.getOverlapPercentage() != null) {
      mission.setOverlapPercentage(request.getOverlapPercentage());
    }
    if (request.getPatternType() != null) {
      mission.setPatternType(request.getPatternType());
    }

    mission = missionRepository.save(mission);
    return MissionResponse.fromEntity(mission);
  }

  /**
   * Delete a mission.
   *
   * @param id the mission ID
   */
  public void deleteMission(UUID id) {
    User currentUser = userService.getCurrentUser();
    Mission mission =
        missionRepository
            .findById(id)
            .filter(m -> m.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + id));

    // Only allow deletion if mission is in PLANNED state
    if (mission.getStatus() != Mission.MissionStatus.PLANNED) {
      throw new BusinessException("Cannot delete mission in " + mission.getStatus() + " state");
    }

    // Release drone
    Drone drone = mission.getDrone();
    drone.setStatus(Drone.DroneStatus.AVAILABLE);
    droneRepository.save(drone);

    missionRepository.delete(mission);
  }

  /**
   * Get missions by status.
   *
   * @param status the mission status
   * @return list of mission responses
   */
  @Transactional(readOnly = true)
  public List<MissionResponse> getMissionsByStatus(Mission.MissionStatus status) {
    User currentUser = userService.getCurrentUser();
    return missionRepository
        .findByOrganizationIdAndStatus(currentUser.getOrganization().getId(), status)
        .stream()
        .map(MissionResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Generate a flight path pattern for a mission.
   *
   * @param missionId the mission ID
   * @param request the pattern generation request
   * @return the flight path response
   */
  public FlightPathResponse generatePattern(UUID missionId, PatternGenerationRequest request) {
    User currentUser = userService.getCurrentUser();
    Mission mission =
        missionRepository
            .findById(missionId)
            .filter(m -> m.getOrganization().getId().equals(currentUser.getOrganization().getId()))
            .orElseThrow(() -> new BusinessException("Mission not found with ID: " + missionId));

    // Only allow pattern generation if mission is in PLANNED state
    if (mission.getStatus() != Mission.MissionStatus.PLANNED) {
      throw new BusinessException(
          "Cannot generate pattern for mission in " + mission.getStatus() + " state");
    }

    SurveyArea surveyArea = mission.getSurveyArea();

    // Generate waypoints based on pattern type
    String waypoints;
    switch (request.getPatternType()) {
      case LINEAR:
        waypoints =
            flightPathService.generateLinearPattern(
                surveyArea, request.getAltitude(), request.getOverlapPercentage());
        break;
      case CROSSHATCH:
        waypoints =
            flightPathService.generateCrosshatchPattern(
                surveyArea, request.getAltitude(), request.getOverlapPercentage());
        break;
      case PERIMETER:
        waypoints =
            flightPathService.generatePerimeterPattern(
                surveyArea, request.getAltitude(), request.getOverlapPercentage());
        break;
      default:
        throw new BusinessException("Unsupported pattern type: " + request.getPatternType());
    }

    // Create or update flight path
    FlightPathRequest flightPathRequest = new FlightPathRequest();
    flightPathRequest.setWaypoints(waypoints);

    // Estimate distance and duration based on waypoints
    // This is a simplified calculation - in a real application, you would calculate this more
    // accurately
    flightPathRequest.setTotalDistance(1000.0); // Default to 1000 meters
    flightPathRequest.setEstimatedDuration(15); // Default to 15 minutes

    // Update mission with pattern type and altitude
    // Convert PatternGenerationRequest.PatternType to Mission.PatternType
    Mission.PatternType missionPatternType;
    switch (request.getPatternType()) {
      case LINEAR:
        missionPatternType = Mission.PatternType.LINEAR;
        break;
      case CROSSHATCH:
        missionPatternType = Mission.PatternType.CROSSHATCH;
        break;
      case PERIMETER:
        missionPatternType = Mission.PatternType.PERIMETER;
        break;
      default:
        throw new BusinessException("Unsupported pattern type: " + request.getPatternType());
    }

    mission.setPatternType(missionPatternType);
    mission.setFlightAltitude(request.getAltitude());
    mission.setOverlapPercentage(request.getOverlapPercentage());
    missionRepository.save(mission);

    return flightPathService.createOrUpdateFlightPath(missionId, flightPathRequest);
  }
}
