package com.flytbase.drone.dto.report;

import com.flytbase.drone.entity.SurveyReport;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for survey report responses. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyReportResponse {

  private UUID id;
  private UUID missionId;
  private String missionName;
  private LocalDateTime generatedAt;
  private String reportType;
  private Double totalArea;
  private Double coveredArea;
  private Integer imageCount;
  private Integer anomaliesDetected;
  private String reportData;
  private Double coveragePercentage;

  /**
   * Convert a SurveyReport entity to a SurveyReportResponse DTO.
   *
   * @param report the survey report entity
   * @return the survey report response DTO
   */
  public static SurveyReportResponse fromEntity(SurveyReport report) {
    SurveyReportResponse response = new SurveyReportResponse();
    response.setId(report.getId());
    response.setMissionId(report.getMission().getId());
    response.setMissionName(report.getMission().getName());
    response.setGeneratedAt(report.getGeneratedAt());
    response.setReportType(report.getReportType());
    response.setTotalArea(report.getTotalArea());
    response.setCoveredArea(report.getCoveredArea());
    response.setImageCount(report.getImageCount());
    response.setAnomaliesDetected(report.getAnomaliesDetected());
    response.setReportData(report.getReportData());

    // Calculate coverage percentage
    if (report.getTotalArea() > 0) {
      response.setCoveragePercentage((report.getCoveredArea() / report.getTotalArea()) * 100);
    } else {
      response.setCoveragePercentage(0.0);
    }

    return response;
  }
}
