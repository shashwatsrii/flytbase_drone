package com.flytbase.drone.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cached mission progress for quick access. */
@Entity
@Table(name = "mission_progress_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionProgressCache {

  @Id private UUID missionId;

  @OneToOne
  @MapsId
  @JoinColumn(name = "mission_id")
  private Mission mission;

  private LocalDateTime startTime;
  private LocalDateTime lastUpdated;
  private Double progressPercentage;
  private Integer waypointsCompleted;
  private Integer totalWaypoints;
  private Double currentLatitude;
  private Double currentLongitude;
  private Double currentAltitude;
  private String flightPath;
}
