package com.flytbase.drone.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for mission count by status. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionStatusCount {
  private String status;
  private Long count;
}
