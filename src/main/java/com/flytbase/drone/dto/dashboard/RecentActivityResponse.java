package com.flytbase.drone.dto.dashboard;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for recent activity dashboard data. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResponse {
  private List<Map<String, Object>> recentMissions;
  private List<Map<String, Object>> unacknowledgedAlerts;
  private List<Map<String, Object>> dailyMissionCounts;
}
