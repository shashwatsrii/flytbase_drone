package com.flytbase.drone.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flytbase.drone.dto.mission.MissionProgressResponse;
import com.flytbase.drone.entity.FlightPath;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.repository.FlightPathRepository;
import com.flytbase.drone.repository.MissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/** Service for handling WebSocket communication for mission monitoring. */
@Service
@Slf4j
public class MissionWebSocketService {

  private final SimpMessagingTemplate messagingTemplate;
  private final MissionRepository missionRepository;
  private final FlightPathRepository flightPathRepository;
  private final ObjectMapper objectMapper;

  @Autowired
  public MissionWebSocketService(
      SimpMessagingTemplate messagingTemplate,
      MissionRepository missionRepository,
      FlightPathRepository flightPathRepository) {
    this.messagingTemplate = messagingTemplate;
    this.missionRepository = missionRepository;
    this.flightPathRepository = flightPathRepository;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Broadcast mission progress update to subscribers.
   *
   * @param missionId the mission ID
   * @param progressResponse the progress response to broadcast
   */
  public void broadcastProgressUpdate(UUID missionId, MissionProgressResponse progressResponse) {
    messagingTemplate.convertAndSend("/topic/missions/" + missionId, progressResponse);
  }

  /** Broadcast mission progress update to subscribers. Overloaded method for compatibility. */
  public void broadcastProgressUpdate(UUID missionId, Object progressResponse) {
    messagingTemplate.convertAndSend("/topic/missions/" + missionId, progressResponse);
  }

  /**
   * Send mission status change notification.
   *
   * @param missionId the mission ID
   * @param status the new status
   * @param message additional message
   */
  public void sendStatusChangeNotification(UUID missionId, String status, String message) {
    StatusChangeNotification notification = new StatusChangeNotification(status, message);
    messagingTemplate.convertAndSend("/topic/missions/" + missionId + "/status", notification);
  }

  /**
   * Simulate drone movement for testing purposes.
   *
   * @param missionId the mission ID
   * @param waypointCount the total number of waypoints (ignored, uses actual waypoints)
   */
  public void simulateDroneMovement(UUID missionId, int waypointCount) {
    // This method simulates a drone moving through the actual mission waypoints

    new Thread(
            () -> {
              try {
                // Get the mission and its flight path
                Mission mission = missionRepository.findById(missionId).orElse(null);
                if (mission == null) {
                  log.error("Mission not found: {}", missionId);
                  return;
                }

                FlightPath flightPath =
                    flightPathRepository.findByMissionId(missionId).orElse(null);
                if (flightPath == null) {
                  log.error("Flight path not found for mission: {}", missionId);
                  return;
                }

                // Parse waypoints from JSON string
                List<Map<String, Double>> waypoints = null;
                try {
                  waypoints =
                      objectMapper.readValue(
                          flightPath.getWaypoints(),
                          new TypeReference<List<Map<String, Double>>>() {});
                } catch (Exception e) {
                  log.error("Failed to parse waypoints: {}", e.getMessage());
                  return;
                }

                if (waypoints == null || waypoints.isEmpty()) {
                  log.error("No waypoints found for mission: {}", missionId);
                  return;
                }

                log.info(
                    "Starting simulation for mission {} with {} waypoints",
                    missionId,
                    waypoints.size());

                // Initial values
                double altitude = mission.getFlightAltitude();
                double speed = mission.getSpeed() != null ? mission.getSpeed() : 10.0;
                int batteryLevel = 100;
                double totalWaypoints = waypoints.size();

                // Simulate movement through actual waypoints
                for (int i = 0; i < waypoints.size(); i++) {
                  // Sleep to simulate time passing
                  Thread.sleep(2000);

                  // Get current waypoint
                  Map<String, Double> waypoint = waypoints.get(i);
                  double lat = waypoint.get("lat");
                  double lng = waypoint.get("lng");
                  double wpAlt = waypoint.getOrDefault("alt", altitude);

                  // Decrease battery level gradually
                  batteryLevel = Math.max(20, 100 - (int) ((i / totalWaypoints) * 80));

                  // Add some realistic variations
                  double actualSpeed = speed + (Math.random() - 0.5) * 2;
                  double actualAlt = wpAlt + (Math.random() - 0.5) * 5;

                  // Create progress response
                  MissionProgressResponse response = new MissionProgressResponse();
                  response.setMissionId(missionId);
                  response.setCurrentWaypointIndex(i);
                  response.setTotalWaypoints((int) totalWaypoints);
                  response.setLatitude(lat);
                  response.setLongitude(lng);
                  response.setAltitude((int) Math.round(actualAlt));
                  response.setSpeed(actualSpeed);
                  response.setBatteryLevel(batteryLevel);
                  response.setCompletionPercentage(((double) (i + 1) / totalWaypoints) * 100);
                  response.setTimestamp(LocalDateTime.now());

                  log.debug(
                      "Broadcasting progress: waypoint {}/{}, lat: {}, lng: {}",
                      i + 1,
                      waypoints.size(),
                      lat,
                      lng);

                  // Send update
                  broadcastProgressUpdate(missionId, response);

                  // If this is the last waypoint, send completion notification
                  if (i == waypoints.size() - 1) {
                    sendStatusChangeNotification(
                        missionId, "COMPLETED", "Mission has been completed successfully");
                  }
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sendStatusChangeNotification(missionId, "ABORTED", "Simulation was interrupted");
              } catch (Exception e) {
                log.error("Error during simulation: {}", e.getMessage(), e);
                sendStatusChangeNotification(
                    missionId, "ABORTED", "Simulation error: " + e.getMessage());
              }
            })
        .start();
  }

  /** Inner class representing a status change notification. */
  private static class StatusChangeNotification {
    private final String status;
    private final String message;

    public StatusChangeNotification(String status, String message) {
      this.status = status;
      this.message = message;
    }

    public String getStatus() {
      return status;
    }

    public String getMessage() {
      return message;
    }
  }
}
