package com.flytbase.drone.dto.mission;

import com.flytbase.drone.entity.Mission;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for mission response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionResponse {

  private UUID id;
  private Long organizationId;
  private UUID droneId;
  private String droneName;
  private UUID surveyAreaId;
  private String surveyAreaName;
  private Long createdById;
  private String createdByName;
  private String name;
  private String description;
  private String type;
  private String status;
  private LocalDateTime scheduledStart;
  private LocalDateTime actualStart;
  private LocalDateTime actualEnd;
  private Integer flightAltitude;
  private Double speed;
  private Integer overlapPercentage;
  private String patternType;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Create a MissionResponse from a Mission entity.
   *
   * @param mission the mission entity
   * @return the mission response DTO
   */
  public static MissionResponse fromEntity(Mission mission) {
    MissionResponse response = new MissionResponse();
    response.setId(mission.getId());
    response.setOrganizationId(mission.getOrganization().getId());
    response.setDroneId(mission.getDrone().getId());
    response.setDroneName(mission.getDrone().getName());
    response.setSurveyAreaId(mission.getSurveyArea().getId());
    response.setSurveyAreaName(mission.getSurveyArea().getName());
    response.setCreatedById(mission.getCreatedBy().getId());
    response.setCreatedByName(mission.getCreatedBy().getFullName());
    response.setName(mission.getName());
    response.setDescription(mission.getDescription());
    response.setType(mission.getType().name());
    response.setStatus(mission.getStatus().name());
    response.setScheduledStart(mission.getScheduledStart());
    response.setActualStart(mission.getActualStart());
    response.setActualEnd(mission.getActualEnd());
    response.setFlightAltitude(mission.getFlightAltitude());
    response.setSpeed(mission.getSpeed());
    response.setOverlapPercentage(mission.getOverlapPercentage());
    response.setPatternType(mission.getPatternType().name());
    response.setCreatedAt(mission.getCreatedAt());
    response.setUpdatedAt(mission.getUpdatedAt());
    return response;
  }
}
