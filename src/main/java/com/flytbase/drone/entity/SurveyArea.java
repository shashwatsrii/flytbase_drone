package com.flytbase.drone.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing a survey area in the system. */
@Entity
@Table(name = "survey_areas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyArea {

  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "boundary_polygon", nullable = false, columnDefinition = "TEXT")
  private String boundaryPolygon;

  @Column(name = "area", nullable = false)
  private Double area;

  @ManyToOne
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

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
