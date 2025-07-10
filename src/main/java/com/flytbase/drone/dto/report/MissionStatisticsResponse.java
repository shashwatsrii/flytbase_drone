package com.flytbase.drone.dto.report;

import com.flytbase.drone.entity.MissionStatistics;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for mission statistics responses. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionStatisticsResponse {

  private UUID id;
  private UUID missionId;
  private String missionName;
  private String missionStatus;
  private BigDecimal totalDistance;
  private Integer actualDuration;
  private BigDecimal averageSpeed;
  private Integer maxAltitude;
  private BigDecimal batteryUsage;
  private Integer waypointsCompleted;
  private Integer totalWaypoints;
  private BigDecimal completionPercentage;
  private LocalDateTime missionStartTime;
  private LocalDateTime missionEndTime;

  /**
   * Convert a MissionStatistics entity to a MissionStatisticsResponse DTO.
   *
   * @param statistics the mission statistics entity
   * @return the mission statistics response DTO
   */
  public static MissionStatisticsResponse fromEntity(MissionStatistics statistics) {
    MissionStatisticsResponse response = new MissionStatisticsResponse();
    response.setId(statistics.getId());
    response.setMissionId(statistics.getMission().getId());
    response.setMissionName(statistics.getMission().getName());
    response.setMissionStatus(statistics.getMission().getStatus().name());
    response.setTotalDistance(statistics.getTotalDistance());
    response.setActualDuration(statistics.getActualDuration());
    response.setAverageSpeed(statistics.getAverageSpeed());
    response.setMaxAltitude(statistics.getMaxAltitude());
    response.setBatteryUsage(statistics.getBatteryUsage());
    response.setWaypointsCompleted(statistics.getWaypointsCompleted());
    response.setTotalWaypoints(statistics.getTotalWaypoints());
    response.setCompletionPercentage(statistics.getCompletionPercentage());
    response.setMissionStartTime(statistics.getMission().getActualStart());
    response.setMissionEndTime(statistics.getMission().getActualEnd());

    return response;
  }
}
