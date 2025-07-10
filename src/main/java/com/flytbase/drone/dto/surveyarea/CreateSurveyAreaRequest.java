package com.flytbase.drone.dto.surveyarea;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for creating a new survey area. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSurveyAreaRequest {

  @NotBlank(message = "Name is required")
  private String name;

  private String description;

  @NotBlank(message = "Boundary polygon is required")
  private String boundaryPolygon;

  @NotNull(message = "Area size is required")
  @Positive(message = "Area size must be positive")
  private Double areaSize;
}
