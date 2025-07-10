package com.flytbase.drone.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

/**
 * Entity representing mission alerts and warnings. Tracks critical events during mission execution.
 */
@Entity
@Table(
    name = "mission_alerts",
    indexes = {
      @Index(name = "idx_alerts_mission", columnList = "mission_id"),
      @Index(name = "idx_alerts_unacknowledged", columnList = "acknowledged")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionAlert {

  public enum AlertType {
    LOW_BATTERY,
    GEOFENCE_BREACH,
    SIGNAL_LOSS,
    GPS_LOSS,
    HIGH_WIND,
    OBSTACLE_DETECTED,
    MOTOR_FAILURE,
    TEMPERATURE_WARNING,
    ALTITUDE_BREACH,
    MISSION_TIMEOUT,
    COMMUNICATION_ERROR
  }

  public enum Severity {
    INFO,
    WARNING,
    CRITICAL
  }

  @Id
  @GeneratedValue
  @Type(type = "org.hibernate.type.UUIDCharType")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Enumerated(EnumType.STRING)
  @Column(name = "alert_type", nullable = false, length = 50)
  private AlertType alertType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Severity severity;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @Column(nullable = false)
  private Boolean acknowledged = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "acknowledged_by")
  private User acknowledgedBy;

  @Column(name = "acknowledged_at")
  private LocalDateTime acknowledgedAt;

  @Column(name = "resolution_notes", columnDefinition = "TEXT")
  private String resolutionNotes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (occurredAt == null) {
      occurredAt = LocalDateTime.now();
    }
  }
}
