package com.flytbase.drone.dto.drone;

import com.flytbase.drone.entity.Drone;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for drone response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DroneResponse {

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LocationDTO {
    private Double latitude;
    private Double longitude;
  }

  private UUID id;
  private Long organizationId;
  private String name;
  private String model;
  private String serialNumber;
  private String status;
  private Integer batteryLevel;
  private LocalDate lastMaintenanceDate;
  private BigDecimal totalFlightHours;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocationDTO homeLocation;

  /**
   * Create a DroneResponse from a Drone entity.
   *
   * @param drone the drone entity
   * @return the drone response DTO
   */
  public static DroneResponse fromEntity(Drone drone) {
    DroneResponse response = new DroneResponse();
    response.setId(drone.getId());
    response.setOrganizationId(drone.getOrganization().getId());
    response.setName(drone.getName());
    response.setModel(drone.getModel());
    response.setSerialNumber(drone.getSerialNumber());
    response.setStatus(drone.getStatus().name());
    response.setBatteryLevel(drone.getBatteryLevel());
    response.setLastMaintenanceDate(drone.getLastMaintenanceDate());
    response.setTotalFlightHours(drone.getTotalFlightHours());
    response.setCreatedAt(drone.getCreatedAt());
    response.setUpdatedAt(drone.getUpdatedAt());

    LocationDTO location = new LocationDTO();
    location.setLatitude(drone.getHomeLocationLatitude());
    location.setLongitude(drone.getHomeLocationLongitude());
    response.setHomeLocation(location);

    return response;
  }
}
