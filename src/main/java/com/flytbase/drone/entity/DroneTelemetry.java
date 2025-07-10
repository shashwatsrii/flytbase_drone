package com.flytbase.drone.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

/**
 * Entity representing drone telemetry data. Stores real-time flight data for analysis and replay.
 */
@Entity
@Table(
    name = "drone_telemetry",
    indexes = {
      @Index(name = "idx_telemetry_mission_timestamp", columnList = "mission_id, timestamp DESC"),
      @Index(name = "idx_telemetry_drone_timestamp", columnList = "drone_id, timestamp DESC")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DroneTelemetry {

  @Id
  @GeneratedValue
  @Type(type = "org.hibernate.type.UUIDCharType")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "drone_id", nullable = false)
  private Drone drone;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  @Column(name = "gps_latitude", nullable = false)
  private Double gpsLatitude;

  @Column(name = "gps_longitude", nullable = false)
  private Double gpsLongitude;

  @Column(name = "gps_altitude", nullable = false)
  private Double gpsAltitude;

  @Column(name = "gps_satellites")
  private Integer gpsSatellites;

  @Column(name = "gps_hdop")
  private Double gpsHdop;

  private Double heading;
  private Double pitch;
  private Double roll;

  @Column(name = "ground_speed")
  private Double groundSpeed;

  @Column(name = "vertical_speed")
  private Double verticalSpeed;

  @Column(name = "battery_voltage")
  private Double batteryVoltage;

  @Column(name = "battery_current")
  private Double batteryCurrent;

  @Column(name = "battery_level")
  private Integer batteryLevel;

  @Column(name = "battery_temperature")
  private Double batteryTemperature;

  @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
  @Column(name = "motor_rpm", columnDefinition = "jsonb")
  private String motorRpm;

  @Column(name = "signal_strength")
  private Integer signalStrength;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }
}
