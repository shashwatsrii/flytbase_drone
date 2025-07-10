package com.flytbase.drone.dto.report;

import java.math.BigDecimal;
import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for creating new mission statistics. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMissionStatisticsRequest {

  @NotNull(message = "Mission ID is required")
  private UUID missionId;

  private BigDecimal totalDistance;

  @Min(value = 0, message = "Actual duration must be positive")
  private Integer actualDuration;

  @Min(value = 0, message = "Average speed must be positive")
  private BigDecimal averageSpeed;

  @Min(value = 0, message = "Max altitude must be positive")
  private Integer maxAltitude;

  @Min(value = 0, message = "Battery usage must be positive")
  private BigDecimal batteryUsage;

  @Min(value = 0, message = "Waypoints completed must be positive")
  private Integer waypointsCompleted;

  @Min(value = 0, message = "Total waypoints must be positive")
  private Integer totalWaypoints;

  private BigDecimal completionPercentage;
}
