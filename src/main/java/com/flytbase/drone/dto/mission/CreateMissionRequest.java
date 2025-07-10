package com.flytbase.drone.dto.mission;

import com.flytbase.drone.entity.Mission;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for creating a new mission. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMissionRequest {

  @NotBlank(message = "Name is required")
  private String name;

  private String description;

  @NotNull(message = "Drone ID is required")
  private UUID droneId;

  @NotNull(message = "Survey area ID is required")
  private UUID surveyAreaId;

  @NotNull(message = "Mission type is required")
  private Mission.MissionType type;

  private LocalDateTime scheduledStart;

  @NotNull(message = "Flight altitude is required")
  @Min(value = 50, message = "Flight altitude must be between 50 and 400 meters")
  @Max(value = 400, message = "Flight altitude must be between 50 and 400 meters")
  private Integer flightAltitude;

  @NotNull(message = "Speed is required")
  @Min(value = 1, message = "Speed must be between 1 and 20 m/s")
  @Max(value = 20, message = "Speed must be between 1 and 20 m/s")
  private Double speed = 10.0;

  @NotNull(message = "Overlap percentage is required")
  @Min(value = 10, message = "Overlap percentage must be between 10 and 90")
  @Max(value = 90, message = "Overlap percentage must be between 10 and 90")
  private Integer overlapPercentage;

  @NotNull(message = "Pattern type is required")
  private Mission.PatternType patternType;
}
