package com.flytbase.drone.dto.report;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for creating a new survey report. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSurveyReportRequest {

  @NotNull(message = "Mission ID is required")
  private UUID missionId;

  private LocalDateTime generatedAt;

  @NotBlank(message = "Report type is required")
  private String reportType;

  @NotNull(message = "Total area is required")
  @Min(value = 0, message = "Total area must be positive")
  private Double totalArea;

  @NotNull(message = "Covered area is required")
  @Min(value = 0, message = "Covered area must be positive")
  private Double coveredArea;

  @NotNull(message = "Image count is required")
  @Min(value = 0, message = "Image count must be positive")
  private Integer imageCount;

  @NotNull(message = "Anomalies detected is required")
  @Min(value = 0, message = "Anomalies detected must be positive")
  private Integer anomaliesDetected;

  private String reportData;
}
