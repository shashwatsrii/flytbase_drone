package com.flytbase.drone.dto.mission;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for sending mission progress updates to clients. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionProgressResponse {

  private UUID id;
  private UUID missionId;
  private String missionName;
  private Integer currentWaypointIndex;
  private Integer totalWaypoints;
  private Double latitude;
  private Double longitude;
  private Integer altitude;
  private Double speed;
  private Integer batteryLevel;
  private String droneStatus;
  private String missionStatus;
  private Double completionPercentage;
  private LocalDateTime timestamp;
}
