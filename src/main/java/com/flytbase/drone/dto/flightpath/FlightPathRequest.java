package com.flytbase.drone.dto.flightpath;

import com.flytbase.drone.entity.Mission;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for flight path generation request. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightPathRequest {

  @NotNull(message = "Pattern type is required")
  private Mission.PatternType patternType;

  @NotBlank(message = "Waypoints are required")
  private String waypoints;

  @NotNull(message = "Total distance is required")
  private Double totalDistance;

  @NotNull(message = "Estimated duration is required")
  private Integer estimatedDuration;
}
