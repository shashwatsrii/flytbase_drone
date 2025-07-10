package com.flytbase.drone.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing a drone in the system. */
@Entity
@Table(name = "drones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drone {

  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String model;

  @Column(name = "serial_number", nullable = false, unique = true)
  private String serialNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DroneStatus status;

  @Column(name = "current_battery_level", nullable = false)
  private Integer batteryLevel;

  @Column(name = "last_maintenance_date")
  private LocalDate lastMaintenanceDate;

  @Column(name = "total_flight_hours", precision = 10, scale = 2)
  private BigDecimal totalFlightHours;

  @Column(name = "home_location_latitude", nullable = false)
  private Double homeLocationLatitude;

  @Column(name = "home_location_longitude", nullable = false)
  private Double homeLocationLongitude;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID();
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) {
      status = DroneStatus.AVAILABLE;
    }
    if (batteryLevel == null) {
      batteryLevel = 100;
    }
    if (totalFlightHours == null) {
      totalFlightHours = BigDecimal.ZERO;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  /** Enum representing drone status in the system. */
  public enum DroneStatus {
    AVAILABLE,
    IN_MISSION,
    MAINTENANCE,
    OFFLINE
  }
}
