package com.flytbase.drone.dto.telemetry;

import com.flytbase.drone.dto.mission.MissionProgressResponse;
import com.flytbase.drone.entity.DroneTelemetry;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for telemetry data responses. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryResponse {

  private UUID id;
  private UUID missionId;
  private UUID droneId;
  private LocalDateTime timestamp;

  // GPS Data
  private Double latitude;
  private Double longitude;
  private Double altitude;
  private Integer gpsSatellites;
  private Double gpsHdop;

  // Orientation
  private Double heading;
  private Double pitch;
  private Double roll;

  // Speed
  private Double groundSpeed;
  private Double verticalSpeed;

  // Battery
  private Double batteryVoltage;
  private Double batteryCurrent;
  private Integer batteryLevel;
  private Double batteryTemperature;

  // Other
  private String motorRpm;
  private Integer signalStrength;

  // Additional computed fields
  private Double progressPercentage;
  private Integer currentWaypointIndex;
  private Double distanceCovered;

  /** Convert entity to DTO. */
  public static TelemetryResponse fromEntity(DroneTelemetry telemetry) {
    TelemetryResponse response = new TelemetryResponse();
    response.setId(telemetry.getId());
    response.setMissionId(telemetry.getMission().getId());
    response.setDroneId(telemetry.getDrone().getId());
    response.setTimestamp(telemetry.getTimestamp());
    response.setLatitude(telemetry.getGpsLatitude());
    response.setLongitude(telemetry.getGpsLongitude());
    response.setAltitude(telemetry.getGpsAltitude());
    response.setGpsSatellites(telemetry.getGpsSatellites());
    response.setGpsHdop(telemetry.getGpsHdop());
    response.setHeading(telemetry.getHeading());
    response.setPitch(telemetry.getPitch());
    response.setRoll(telemetry.getRoll());
    response.setGroundSpeed(telemetry.getGroundSpeed());
    response.setVerticalSpeed(telemetry.getVerticalSpeed());
    response.setBatteryVoltage(telemetry.getBatteryVoltage());
    response.setBatteryCurrent(telemetry.getBatteryCurrent());
    response.setBatteryLevel(telemetry.getBatteryLevel());
    response.setBatteryTemperature(telemetry.getBatteryTemperature());
    response.setMotorRpm(telemetry.getMotorRpm());
    response.setSignalStrength(telemetry.getSignalStrength());
    return response;
  }

  /** Convert to MissionProgressResponse for WebSocket. */
  public MissionProgressResponse toProgressResponse() {
    MissionProgressResponse progress = new MissionProgressResponse();
    progress.setMissionId(this.missionId);
    progress.setCurrentWaypointIndex(
        this.currentWaypointIndex != null ? this.currentWaypointIndex : 0);
    progress.setTotalWaypoints(20); // This should come from mission data
    progress.setLatitude(this.latitude);
    progress.setLongitude(this.longitude);
    progress.setAltitude(this.altitude != null ? this.altitude.intValue() : 0);
    progress.setSpeed(this.groundSpeed != null ? this.groundSpeed : 0.0);
    progress.setBatteryLevel(this.batteryLevel);
    progress.setCompletionPercentage(
        this.progressPercentage != null ? this.progressPercentage : 0.0);
    progress.setTimestamp(this.timestamp);
    return progress;
  }
}
