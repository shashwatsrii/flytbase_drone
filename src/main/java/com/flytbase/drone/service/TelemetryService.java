package com.flytbase.drone.service;

import com.flytbase.drone.dto.telemetry.TelemetryRequest;
import com.flytbase.drone.dto.telemetry.TelemetryResponse;
import com.flytbase.drone.entity.DroneTelemetry;
import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.MissionAlert;
import com.flytbase.drone.entity.MissionProgressCache;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling drone telemetry data. Manages real-time data persistence and retrieval. */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TelemetryService {

  private final DroneTelemetryRepository telemetryRepository;
  private final MissionRepository missionRepository;
  private final DroneRepository droneRepository;
  private final MissionProgressCacheRepository progressCacheRepository;
  private final MissionAlertRepository alertRepository;
  private final MissionWebSocketService webSocketService;

  /** Record telemetry data from drone. */
  public TelemetryResponse recordTelemetry(UUID missionId, TelemetryRequest request) {
    Mission mission =
        missionRepository
            .findById(missionId)
            .orElseThrow(() -> new BusinessException("Mission not found"));

    if (mission.getStatus() != Mission.MissionStatus.ACTIVE) {
      throw new BusinessException("Cannot record telemetry for inactive mission");
    }

    // Create telemetry record
    DroneTelemetry telemetry = new DroneTelemetry();
    telemetry.setMission(mission);
    telemetry.setDrone(mission.getDrone());
    telemetry.setTimestamp(LocalDateTime.now());
    telemetry.setGpsLatitude(request.getLatitude());
    telemetry.setGpsLongitude(request.getLongitude());
    telemetry.setGpsAltitude(request.getAltitude());
    telemetry.setGpsSatellites(request.getGpsSatellites());
    telemetry.setGpsHdop(request.getGpsHdop());
    telemetry.setHeading(request.getHeading());
    telemetry.setPitch(request.getPitch());
    telemetry.setRoll(request.getRoll());
    telemetry.setGroundSpeed(request.getGroundSpeed());
    telemetry.setVerticalSpeed(request.getVerticalSpeed());
    telemetry.setBatteryVoltage(request.getBatteryVoltage());
    telemetry.setBatteryCurrent(request.getBatteryCurrent());
    telemetry.setBatteryLevel(request.getBatteryLevel());
    telemetry.setBatteryTemperature(request.getBatteryTemperature());
    telemetry.setMotorRpm(request.getMotorRpm());
    telemetry.setSignalStrength(request.getSignalStrength());

    telemetry = telemetryRepository.save(telemetry);

    // Update mission progress
    updateMissionProgress(mission, telemetry);

    // Check for alerts
    checkAndCreateAlerts(mission, telemetry);

    // Broadcast via WebSocket
    broadcastTelemetry(mission, telemetry);

    return TelemetryResponse.fromEntity(telemetry);
  }

  /** Get telemetry history for a mission. */
  @Transactional(readOnly = true)
  public Page<TelemetryResponse> getTelemetryHistory(
      UUID missionId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {

    return telemetryRepository
        .findByMissionIdAndTimestampBetweenOrderByTimestampDesc(
            missionId, startTime, endTime, pageable)
        .map(TelemetryResponse::fromEntity);
  }

  /** Get latest telemetry for a mission. */
  @Transactional(readOnly = true)
  public TelemetryResponse getLatestTelemetry(UUID missionId) {
    DroneTelemetry latest = telemetryRepository.findTopByMissionIdOrderByTimestampDesc(missionId);
    if (latest == null) {
      throw new BusinessException("No telemetry data found for mission");
    }
    return TelemetryResponse.fromEntity(latest);
  }

  /** Update mission progress based on telemetry. */
  private void updateMissionProgress(Mission mission, DroneTelemetry telemetry) {
    MissionProgressCache progress =
        progressCacheRepository
            .findByMissionId(mission.getId())
            .orElseGet(
                () -> {
                  MissionProgressCache newProgress = new MissionProgressCache();
                  newProgress.setMissionId(mission.getId());
                  newProgress.setMission(mission);
                  newProgress.setStartTime(LocalDateTime.now());
                  return newProgress;
                });

    // Update current position
    progress.setCurrentLatitude(telemetry.getGpsLatitude());
    progress.setCurrentLongitude(telemetry.getGpsLongitude());
    progress.setCurrentAltitude(telemetry.getGpsAltitude());
    progress.setLastUpdated(LocalDateTime.now());

    // Calculate progress percentage based on waypoints
    // This is simplified - in production, you'd calculate based on actual flight path
    // For now, always calculate progress
    double progressPercentage = calculateProgressPercentage(mission, telemetry);
    progress.setProgressPercentage(progressPercentage);

    progressCacheRepository.save(progress);
  }

  /** Check telemetry data for alert conditions. */
  private void checkAndCreateAlerts(Mission mission, DroneTelemetry telemetry) {
    // Low battery alert
    if (telemetry.getBatteryLevel() < 20) {
      createAlert(
          mission,
          MissionAlert.AlertType.LOW_BATTERY,
          MissionAlert.Severity.WARNING,
          "Battery level critical: " + telemetry.getBatteryLevel() + "%");
    }

    // GPS signal loss
    if (telemetry.getGpsSatellites() != null && telemetry.getGpsSatellites() < 6) {
      createAlert(
          mission,
          MissionAlert.AlertType.GPS_LOSS,
          MissionAlert.Severity.WARNING,
          "GPS signal weak: " + telemetry.getGpsSatellites() + " satellites");
    }

    // Altitude breach
    if (telemetry.getGpsAltitude() > mission.getFlightAltitude() + 50) {
      createAlert(
          mission,
          MissionAlert.AlertType.ALTITUDE_BREACH,
          MissionAlert.Severity.CRITICAL,
          "Altitude exceeded: "
              + telemetry.getGpsAltitude()
              + "m (limit: "
              + mission.getFlightAltitude()
              + "m)");
    }

    // Signal strength
    if (telemetry.getSignalStrength() != null && telemetry.getSignalStrength() < 30) {
      createAlert(
          mission,
          MissionAlert.AlertType.SIGNAL_LOSS,
          MissionAlert.Severity.WARNING,
          "Weak signal strength: " + telemetry.getSignalStrength() + "%");
    }
  }

  /** Create an alert if it doesn't already exist. */
  private void createAlert(
      Mission mission,
      MissionAlert.AlertType type,
      MissionAlert.Severity severity,
      String message) {
    // Check if similar alert was created recently (within last 5 minutes)
    LocalDateTime recentTime = LocalDateTime.now().minusMinutes(5);
    boolean recentAlertExists =
        alertRepository.existsByMissionIdAndAlertTypeAndOccurredAtAfter(
            mission.getId(), type, recentTime);

    if (!recentAlertExists) {
      MissionAlert alert = new MissionAlert();
      alert.setMission(mission);
      alert.setAlertType(type);
      alert.setSeverity(severity);
      alert.setMessage(message);
      alert.setOccurredAt(LocalDateTime.now());

      alertRepository.save(alert);

      // Broadcast alert via WebSocket
      webSocketService.sendStatusChangeNotification(mission.getId(), "ALERT", message);
    }
  }

  /** Broadcast telemetry via WebSocket. */
  private void broadcastTelemetry(Mission mission, DroneTelemetry telemetry) {
    // Create response for WebSocket
    TelemetryResponse response = TelemetryResponse.fromEntity(telemetry);

    // Add mission progress info
    MissionProgressCache progress =
        progressCacheRepository.findByMissionId(mission.getId()).orElse(null);
    if (progress != null) {
      response.setProgressPercentage(
          progress.getProgressPercentage() != null ? progress.getProgressPercentage() : 0.0);
    }

    // Send via WebSocket service
    webSocketService.broadcastProgressUpdate(mission.getId(), response.toProgressResponse());
  }

  /** Calculate progress percentage based on distance. */
  private double calculateProgressPercentage(Mission mission, DroneTelemetry telemetry) {
    // This is a simplified calculation
    // In production, you'd calculate based on:
    // - Distance along planned route
    // - Waypoints reached
    // - Area covered for survey missions

    // For now, return a mock calculation
    long telemetryCount = telemetryRepository.countByMissionId(mission.getId());
    return Math.min(100.0, (telemetryCount * 2.0)); // Rough estimate
  }

  /** Clean up old telemetry data. */
  public void cleanupOldTelemetry(int daysToKeep) {
    LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
    telemetryRepository.deleteByTimestampBefore(cutoffTime);
    log.info("Deleted telemetry data older than {} days", daysToKeep);
  }
}
