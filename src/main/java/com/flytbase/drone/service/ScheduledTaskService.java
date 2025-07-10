package com.flytbase.drone.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Service for scheduled background tasks. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

  private final JdbcTemplate jdbcTemplate;
  private final TelemetryService telemetryService;

  /** Refresh dashboard statistics materialized view. Runs every 5 minutes. */
  @Scheduled(fixedDelay = 300000) // 5 minutes
  public void refreshDashboardStatistics() {
    try {
      jdbcTemplate.execute("SELECT refresh_dashboard_statistics()");
      log.debug("Dashboard statistics refreshed");
    } catch (Exception e) {
      log.error("Failed to refresh dashboard statistics", e);
    }
  }

  /** Clean up old telemetry data. Runs daily at 2 AM. */
  @Scheduled(cron = "0 0 2 * * *")
  public void cleanupOldTelemetry() {
    try {
      // Keep telemetry data for 30 days
      telemetryService.cleanupOldTelemetry(30);
      log.info("Old telemetry data cleaned up");
    } catch (Exception e) {
      log.error("Failed to cleanup old telemetry", e);
    }
  }

  /** Check for stale WebSocket sessions. Runs every minute. */
  @Scheduled(fixedDelay = 60000) // 1 minute
  public void cleanupStaleSessions() {
    try {
      // Mark sessions as disconnected if no ping in last 5 minutes
      String sql =
          "UPDATE websocket_sessions SET disconnected_at = CURRENT_TIMESTAMP "
              + "WHERE disconnected_at IS NULL AND last_ping < CURRENT_TIMESTAMP - INTERVAL '5 minutes'";
      int updated = jdbcTemplate.update(sql);
      if (updated > 0) {
        log.info("Marked {} stale WebSocket sessions as disconnected", updated);
      }
    } catch (Exception e) {
      log.error("Failed to cleanup stale sessions", e);
    }
  }
}
