package com.flytbase.drone.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for dashboard statistics. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
  private Integer totalDrones;
  private Integer activeMissions;
  private Integer completedMissions;
  private Integer abortedMissions;
  private Double totalFlightHours;
  private Integer totalSurveyAreas;
  private Double totalAreaCoverage;
  private Double successRate;
  private Integer unacknowledgedAlerts;
}
