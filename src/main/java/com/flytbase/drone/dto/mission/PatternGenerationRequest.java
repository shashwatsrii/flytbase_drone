package com.flytbase.drone.dto.mission;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for pattern generation request. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatternGenerationRequest {

  /** The type of pattern to generate. */
  public enum PatternType {
    LINEAR,
    CROSSHATCH,
    PERIMETER
  }

  /** The type of pattern to generate. */
  @NotNull(message = "Pattern type is required")
  private PatternType patternType;

  /** The altitude at which the drone should fly (in meters). */
  @NotNull(message = "Altitude is required")
  @Min(value = 10, message = "Altitude must be at least 10 meters")
  @Max(value = 500, message = "Altitude must not exceed 500 meters")
  private Integer altitude;

  /** The overlap percentage between flight lines (0-100). */
  @NotNull(message = "Overlap percentage is required")
  @Min(value = 0, message = "Overlap percentage must be at least 0")
  @Max(value = 100, message = "Overlap percentage must not exceed 100")
  private Integer overlapPercentage;
}
