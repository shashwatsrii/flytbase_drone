package com.flytbase.drone.dto.mission;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for receiving mission progress updates from clients. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionProgressRequest {

  private UUID missionId;
  private Integer currentWaypointIndex;
  private Double latitude;
  private Double longitude;
  private Integer altitude;
  private Double speed;
  private Integer batteryLevel;
  private LocalDateTime timestamp;
}
