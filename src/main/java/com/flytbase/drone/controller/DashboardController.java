package com.flytbase.drone.controller;

import com.flytbase.drone.dto.dashboard.DashboardStatsResponse;
import com.flytbase.drone.dto.dashboard.MissionStatusCount;
import com.flytbase.drone.dto.dashboard.RecentActivityResponse;
import com.flytbase.drone.service.DashboardService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for dashboard data endpoints. */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  /**
   * Get dashboard statistics.
   *
   * @return dashboard statistics
   */
  @GetMapping("/stats")
  public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
    return ResponseEntity.ok(dashboardService.getDashboardStats());
  }

  /**
   * Get mission distribution by status.
   *
   * @return list of mission counts by status
   */
  @GetMapping("/missions-by-status")
  public ResponseEntity<List<MissionStatusCount>> getMissionsByStatus() {
    return ResponseEntity.ok(dashboardService.getMissionsByStatus());
  }

  /**
   * Get recent activity.
   *
   * @return recent activity data
   */
  @GetMapping("/recent-activity")
  public ResponseEntity<RecentActivityResponse> getRecentActivity() {
    return ResponseEntity.ok(dashboardService.getRecentActivity());
  }

  /**
   * Get fleet utilization statistics.
   *
   * @return fleet utilization data
   */
  @GetMapping("/fleet-utilization")
  public ResponseEntity<Map<String, Object>> getFleetUtilization() {
    return ResponseEntity.ok(dashboardService.getFleetUtilization());
  }
}
