package com.flytbase.drone.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity representing a survey report in the system. */
@Entity
@Table(name = "survey_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyReport {

  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Column(name = "generated_at", nullable = false)
  private LocalDateTime generatedAt;

  @Column(name = "report_type", nullable = false)
  private String reportType;

  @Column(name = "total_area", nullable = false)
  private Double totalArea;

  @Column(name = "covered_area", nullable = false)
  private Double coveredArea;

  @Column(name = "image_count", nullable = false)
  private Integer imageCount;

  @Column(name = "anomalies_detected", nullable = false)
  private Integer anomaliesDetected;

  @Column(name = "report_data", columnDefinition = "TEXT")
  private String reportData;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    id = UUID.randomUUID();
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (generatedAt == null) {
      generatedAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
