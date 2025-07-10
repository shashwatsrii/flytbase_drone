package com.flytbase.drone.dto.drone;

import com.flytbase.drone.entity.Drone;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for updating an existing drone. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDroneRequest {

  private String name;

  private String model;

  private Drone.DroneStatus status;

  @Min(value = 0, message = "Battery level must be between 0 and 100")
  @Max(value = 100, message = "Battery level must be between 0 and 100")
  private Integer batteryLevel;

  private LocalDate lastMaintenanceDate;

  @Min(value = 0, message = "Total flight hours cannot be negative")
  private BigDecimal totalFlightHours;
}
