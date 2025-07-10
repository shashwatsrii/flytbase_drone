package com.flytbase.drone.controller;

import com.flytbase.drone.dto.mission.MissionProgressRequest;
import com.flytbase.drone.dto.mission.MissionProgressResponse;
import com.flytbase.drone.service.MissionProgressService;
import com.flytbase.drone.service.MissionWebSocketService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/** Controller for handling WebSocket communication for mission monitoring. */
@Controller
public class MissionWebSocketController {

  private final MissionProgressService missionProgressService;
  private final MissionWebSocketService missionWebSocketService;

  @Autowired
  public MissionWebSocketController(
      MissionProgressService missionProgressService,
      MissionWebSocketService missionWebSocketService) {
    this.missionProgressService = missionProgressService;
    this.missionWebSocketService = missionWebSocketService;
  }

  /**
   * Handle mission progress updates from clients.
   *
   * @param missionId the mission ID
   * @param request the progress update request
   * @return the progress response
   */
  @MessageMapping("/missions/{missionId}/progress")
  @SendTo("/topic/missions/{missionId}")
  public MissionProgressResponse handleProgressUpdate(
      @DestinationVariable UUID missionId, MissionProgressRequest request) {
    // Ensure the missionId in the path matches the one in the request
    if (!missionId.equals(request.getMissionId())) {
      request.setMissionId(missionId);
    }

    // Record the progress and get the response
    MissionProgressResponse response = missionProgressService.recordProgress(request);

    // Broadcast the update to all subscribers
    missionWebSocketService.broadcastProgressUpdate(missionId, response);

    return response;
  }

  /**
   * Handle telemetry data from drones.
   *
   * @param missionId the mission ID
   * @param request the telemetry data
   */
  @MessageMapping("/missions/{missionId}/telemetry")
  public void handleTelemetryData(
      @DestinationVariable UUID missionId, MissionProgressRequest request) {
    // Ensure the missionId in the path matches the one in the request
    if (!missionId.equals(request.getMissionId())) {
      request.setMissionId(missionId);
    }

    // Process telemetry data (same as progress for now)
    MissionProgressResponse response = missionProgressService.recordProgress(request);

    // Broadcast the update to all subscribers
    missionWebSocketService.broadcastProgressUpdate(missionId, response);
  }
}
