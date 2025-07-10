package com.flytbase.drone.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing a flight path in the system. */
@Entity
@Table(name = "flight_paths")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightPath {

  @Id @GeneratedValue private UUID id;

  @OneToOne
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Column(name = "waypoints", nullable = false, columnDefinition = "TEXT")
  private String waypoints;

  @Column(name = "total_distance", precision = 15, scale = 2)
  private BigDecimal totalDistance;

  @Column(name = "estimated_duration")
  private Integer estimatedDuration;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID();
    createdAt = LocalDateTime.now();
  }
}
