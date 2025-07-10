package com.flytbase.drone.service;

import com.flytbase.drone.dto.mission.MissionProgressResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/** Service for handling WebSocket communication for mission monitoring. */
@Service
public class MissionWebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  @Autowired
  public MissionWebSocketService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
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
   * @param waypointCount the total number of waypoints
   */
  public void simulateDroneMovement(UUID missionId, int waypointCount) {
    // This method is used for testing the real-time updates
    // It simulates a drone moving through waypoints and sends updates

    // Create a new thread to simulate movement
    new Thread(
            () -> {
              try {
                // Start with initial values
                double startLat = 37.7749;
                double startLon = -122.4194;
                int altitude = 100;
                double speed = 5.0;
                int batteryLevel = 100;

                // Simulate movement through waypoints
                for (int i = 0; i < waypointCount; i++) {
                  // Sleep to simulate time passing
                  Thread.sleep(2000);

                  // Update position (simple simulation)
                  double lat = startLat + (i * 0.0001);
                  double lon = startLon + (i * 0.0001);

                  // Decrease battery level gradually
                  batteryLevel = Math.max(0, batteryLevel - 1);

                  // Create progress response
                  MissionProgressResponse response = new MissionProgressResponse();
                  response.setMissionId(missionId);
                  response.setCurrentWaypointIndex(i);
                  response.setTotalWaypoints(waypointCount);
                  response.setLatitude(lat);
                  response.setLongitude(lon);
                  response.setAltitude(altitude);
                  response.setSpeed(speed);
                  response.setBatteryLevel(batteryLevel);
                  response.setCompletionPercentage((double) i / waypointCount * 100);
                  response.setTimestamp(LocalDateTime.now());

                  // Send update
                  broadcastProgressUpdate(missionId, response);

                  // If this is the last waypoint, send completion notification
                  if (i == waypointCount - 1) {
                    sendStatusChangeNotification(
                        missionId, "COMPLETED", "Mission has been completed successfully");
                  }
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sendStatusChangeNotification(missionId, "ABORTED", "Simulation was interrupted");
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
