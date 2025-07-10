package com.flytbase.drone.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing mission statistics in the system. */
@Entity
@Table(name = "mission_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionStatistics {

  @Id @GeneratedValue private UUID id;

  @OneToOne
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Column(name = "total_distance", precision = 15, scale = 2)
  private BigDecimal totalDistance;

  @Column(name = "actual_duration")
  private Integer actualDuration;

  @Column(name = "average_speed", precision = 10, scale = 2)
  private BigDecimal averageSpeed;

  @Column(name = "max_altitude")
  private Integer maxAltitude;

  @Column(name = "battery_usage", precision = 5, scale = 2)
  private BigDecimal batteryUsage;

  @Column(name = "waypoints_completed")
  private Integer waypointsCompleted;

  @Column(name = "total_waypoints")
  private Integer totalWaypoints;

  @Column(name = "completion_percentage", precision = 5, scale = 2)
  private BigDecimal completionPercentage;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID();
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
