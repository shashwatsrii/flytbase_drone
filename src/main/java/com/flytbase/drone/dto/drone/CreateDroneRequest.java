package com.flytbase.drone.dto.drone;

import com.flytbase.drone.entity.Drone;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for creating a new drone. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDroneRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Model is required")
  private String model;

  @NotBlank(message = "Serial number is required")
  private String serialNumber;

  @NotNull(message = "Status is required")
  private Drone.DroneStatus status = Drone.DroneStatus.AVAILABLE;

  @NotNull(message = "Battery level is required")
  @Min(value = 0, message = "Battery level must be between 0 and 100")
  @Max(value = 100, message = "Battery level must be between 0 and 100")
  private Integer batteryLevel;

  private LocalDate lastMaintenanceDate;

  @NotNull(message = "Total flight hours is required")
  @Min(value = 0, message = "Total flight hours cannot be negative")
  private BigDecimal totalFlightHours = BigDecimal.ZERO;

  @NotNull(message = "Home location is required")
  private LocationDTO homeLocation;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LocationDTO {
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
  }
}
