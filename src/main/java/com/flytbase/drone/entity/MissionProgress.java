package com.flytbase.drone.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing the real-time progress of a mission in the system. */
@Entity
@Table(name = "mission_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionProgress {

  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Column(name = "current_waypoint_index", nullable = false)
  private Integer currentWaypointIndex;

  @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
  private Double latitude;

  @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
  private Double longitude;

  @Column(name = "altitude", nullable = false)
  private Integer altitude;

  @Column(name = "speed", nullable = false)
  private Double speed;

  @Column(name = "battery_level", nullable = false)
  private Integer batteryLevel;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID();
    createdAt = LocalDateTime.now();
    if (timestamp == null) {
      timestamp = LocalDateTime.now();
    }
  }
}
