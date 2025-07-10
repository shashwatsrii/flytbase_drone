package com.flytbase.drone.dto.mission;

import com.flytbase.drone.entity.Mission;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for updating an existing mission. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMissionRequest {

  private String name;

  private String description;

  private UUID droneId;

  private UUID surveyAreaId;

  private Mission.MissionType type;

  private Mission.MissionStatus status;

  private LocalDateTime scheduledStart;

  @Min(value = 50, message = "Flight altitude must be between 50 and 400 meters")
  @Max(value = 400, message = "Flight altitude must be between 50 and 400 meters")
  private Integer flightAltitude;

  @Min(value = 1, message = "Speed must be between 1 and 30 m/s")
  @Max(value = 30, message = "Speed must be between 1 and 30 m/s")
  private Double speed;

  @Min(value = 10, message = "Overlap percentage must be between 10 and 90")
  @Max(value = 90, message = "Overlap percentage must be between 10 and 90")
  private Integer overlapPercentage;

  private Mission.PatternType patternType;
}
