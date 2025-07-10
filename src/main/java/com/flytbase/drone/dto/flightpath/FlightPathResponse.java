package com.flytbase.drone.dto.flightpath;

import com.flytbase.drone.entity.FlightPath;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for flight path response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightPathResponse {

  private UUID id;
  private UUID missionId;
  private String waypoints;
  private BigDecimal totalDistance;
  private Integer estimatedDuration;
  private LocalDateTime createdAt;

  /**
   * Create a FlightPathResponse from a FlightPath entity.
   *
   * @param flightPath the flight path entity
   * @return the flight path response DTO
   */
  public static FlightPathResponse fromEntity(FlightPath flightPath) {
    FlightPathResponse response = new FlightPathResponse();
    response.setId(flightPath.getId());
    response.setMissionId(flightPath.getMission().getId());
    response.setWaypoints(flightPath.getWaypoints());
    response.setTotalDistance(flightPath.getTotalDistance());
    response.setEstimatedDuration(flightPath.getEstimatedDuration());
    response.setCreatedAt(flightPath.getCreatedAt());
    return response;
  }
}
