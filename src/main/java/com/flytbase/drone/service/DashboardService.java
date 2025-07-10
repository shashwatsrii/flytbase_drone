package com.flytbase.drone.service;

import com.flytbase.drone.dto.dashboard.DashboardStatsResponse;
import com.flytbase.drone.dto.dashboard.MissionStatusCount;
import com.flytbase.drone.dto.dashboard.RecentActivityResponse;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.MissionAlert;
import com.flytbase.drone.entity.User;
import com.flytbase.drone.repository.MissionAlertRepository;
import com.flytbase.drone.repository.MissionRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for dashboard data aggregation. Uses materialized views and optimized queries for
 * performance.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

  private final JdbcTemplate jdbcTemplate;
  private final MissionRepository missionRepository;
  private final MissionAlertRepository alertRepository;
  private final UserService userService;

  /** Get dashboard statistics from materialized view. */
  public DashboardStatsResponse getDashboardStats() {
    User currentUser = userService.getCurrentUser();
    Long orgId = currentUser.getOrganization().getId();

    String sql = "SELECT * FROM dashboard_statistics WHERE organization_id = ?";

    List<DashboardStatsResponse> results =
        jdbcTemplate.query(
            sql,
            new Object[] {orgId},
            (rs, rowNum) -> {
              DashboardStatsResponse stats = new DashboardStatsResponse();
              stats.setTotalDrones(rs.getInt("total_drones"));
              stats.setActiveMissions(rs.getInt("active_missions"));
              stats.setCompletedMissions(rs.getInt("completed_missions"));
              stats.setAbortedMissions(rs.getInt("aborted_missions"));
              stats.setTotalFlightHours(rs.getDouble("total_flight_hours"));
              stats.setTotalSurveyAreas(rs.getInt("total_survey_areas"));
              stats.setTotalAreaCoverage(rs.getDouble("total_area_coverage"));

              // Calculate success rate
              int totalFinished = stats.getCompletedMissions() + stats.getAbortedMissions();
              if (totalFinished > 0) {
                stats.setSuccessRate((double) stats.getCompletedMissions() / totalFinished * 100);
              } else {
                stats.setSuccessRate(0.0);
              }

              return stats;
            });

    if (results.isEmpty()) {
      // Return default empty stats if no data exists
      return new DashboardStatsResponse(0, 0, 0, 0, 0.0, 0, 0.0, 0.0, 0);
    }

    return results.get(0);
  }

  /** Get mission distribution by status. */
  public List<MissionStatusCount> getMissionsByStatus() {
    User currentUser = userService.getCurrentUser();

    String sql =
        "SELECT status, COUNT(*) as count FROM missions "
            + "WHERE organization_id = ? GROUP BY status";

    return jdbcTemplate.query(
        sql,
        new Object[] {currentUser.getOrganization().getId()},
        (rs, rowNum) -> new MissionStatusCount(rs.getString("status"), rs.getLong("count")));
  }

  /** Get recent activity for dashboard. */
  public RecentActivityResponse getRecentActivity() {
    User currentUser = userService.getCurrentUser();
    LocalDateTime since = LocalDateTime.now().minusDays(7);

    RecentActivityResponse activity = new RecentActivityResponse();

    // Recent missions
    List<Mission> recentMissions =
        missionRepository.findTop10ByOrganizationIdOrderByCreatedAtDesc(
            currentUser.getOrganization().getId());
    activity.setRecentMissions(
        recentMissions.stream().map(this::mapToMissionSummary).collect(Collectors.toList()));

    // Recent alerts
    List<MissionAlert> recentAlerts =
        alertRepository.findByAcknowledgedFalseOrderBySeverityDescOccurredAtDesc();
    activity.setUnacknowledgedAlerts(
        recentAlerts.stream().map(this::mapToAlertSummary).collect(Collectors.toList()));

    // Daily mission counts for last 7 days
    String dailySql =
        "SELECT DATE(scheduled_start) as date, COUNT(*) as count "
            + "FROM missions WHERE organization_id = ? "
            + "AND scheduled_start >= ? "
            + "GROUP BY DATE(scheduled_start) ORDER BY date";

    List<Map<String, Object>> dailyCounts =
        jdbcTemplate.queryForList(dailySql, currentUser.getOrganization().getId(), since);
    activity.setDailyMissionCounts(dailyCounts);

    return activity;
  }

  /** Get fleet utilization statistics. */
  public Map<String, Object> getFleetUtilization() {
    User currentUser = userService.getCurrentUser();

    String sql =
        "SELECT "
            + "COUNT(*) as total_drones, "
            + "COUNT(*) FILTER (WHERE status = 'IN_MISSION') as active_drones, "
            + "COUNT(*) FILTER (WHERE status = 'MAINTENANCE') as maintenance_drones, "
            + "AVG(total_flight_hours) as avg_flight_hours "
            + "FROM drones WHERE organization_id = ?";

    return jdbcTemplate.queryForMap(sql, currentUser.getOrganization().getId());
  }

  private Map<String, Object> mapToMissionSummary(Mission mission) {
    Map<String, Object> summary = new HashMap<>();
    summary.put("id", mission.getId());
    summary.put("name", mission.getName());
    summary.put("status", mission.getStatus());
    summary.put("droneName", mission.getDrone().getName());
    summary.put("scheduledStart", mission.getScheduledStart());
    summary.put("createdAt", mission.getCreatedAt());
    return summary;
  }

  private Map<String, Object> mapToAlertSummary(MissionAlert alert) {
    Map<String, Object> summary = new HashMap<>();
    summary.put("id", alert.getId());
    summary.put("missionName", alert.getMission().getName());
    summary.put("alertType", alert.getAlertType());
    summary.put("severity", alert.getSeverity());
    summary.put("message", alert.getMessage());
    summary.put("occurredAt", alert.getOccurredAt());
    return summary;
  }
}
