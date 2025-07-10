package com.flytbase.drone.dto.surveyarea;

import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for updating an existing survey area. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSurveyAreaRequest {

  private String name;

  private String description;

  private String boundaryPolygon;

  @Positive(message = "Area size must be positive")
  private Double areaSize;
}
