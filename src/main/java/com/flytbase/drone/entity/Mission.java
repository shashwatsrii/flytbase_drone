package com.flytbase.drone.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing a mission in the system. */
@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mission {

  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @ManyToOne
  @JoinColumn(name = "drone_id", nullable = false)
  private Drone drone;

  @ManyToOne
  @JoinColumn(name = "survey_area_id", nullable = false)
  private SurveyArea surveyArea;

  @ManyToOne
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MissionType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MissionStatus status;

  @Column(name = "scheduled_start")
  private LocalDateTime scheduledStart;

  @Column(name = "actual_start")
  private LocalDateTime actualStart;

  @Column(name = "actual_end")
  private LocalDateTime actualEnd;

  @Column(name = "flight_altitude", nullable = false)
  private Integer flightAltitude;

  @Column(nullable = false)
  private Double speed;

  @Column(name = "overlap_percentage", nullable = false)
  private Integer overlapPercentage;

  @Enumerated(EnumType.STRING)
  @Column(name = "pattern_type", nullable = false)
  private PatternType patternType;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID();
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (status == null) {
      status = MissionStatus.PLANNED;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  /** Enum representing mission type in the system. */
  public enum MissionType {
    INSPECTION,
    SECURITY,
    MAPPING
  }

  /** Enum representing mission status in the system. */
  public enum MissionStatus {
    PLANNED,
    ACTIVE,
    PAUSED,
    COMPLETED,
    ABORTED
  }

  /** Enum representing flight pattern type in the system. */
  public enum PatternType {
    LINEAR,
    CROSSHATCH,
    PERIMETER
  }
}
