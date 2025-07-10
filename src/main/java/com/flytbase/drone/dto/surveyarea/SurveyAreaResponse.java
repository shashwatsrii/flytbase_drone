package com.flytbase.drone.dto.surveyarea;

import com.flytbase.drone.entity.SurveyArea;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for survey area response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAreaResponse {

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreatedByDTO {
    private Long id;
    private String username;
  }

  private UUID id;
  private Long organizationId;
  private String name;
  private String description;
  private String boundaryPolygon;
  private Double area;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private CreatedByDTO createdBy;

  /**
   * Create a SurveyAreaResponse from a SurveyArea entity.
   *
   * @param surveyArea the survey area entity
   * @return the survey area response DTO
   */
  public static SurveyAreaResponse fromEntity(SurveyArea surveyArea) {
    SurveyAreaResponse response = new SurveyAreaResponse();
    response.setId(surveyArea.getId());
    response.setOrganizationId(surveyArea.getOrganization().getId());
    response.setName(surveyArea.getName());
    response.setDescription(surveyArea.getDescription());
    response.setBoundaryPolygon(surveyArea.getBoundaryPolygon());
    response.setArea(surveyArea.getArea());
    response.setCreatedAt(surveyArea.getCreatedAt());
    response.setUpdatedAt(surveyArea.getUpdatedAt());

    CreatedByDTO createdBy = new CreatedByDTO();
    createdBy.setId(surveyArea.getCreatedBy().getId());
    createdBy.setUsername(surveyArea.getCreatedBy().getEmail());
    response.setCreatedBy(createdBy);

    return response;
  }
}
