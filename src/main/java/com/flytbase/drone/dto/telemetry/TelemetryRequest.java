package com.flytbase.drone.dto.telemetry;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for telemetry data received from drone. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryRequest {

  @NotNull(message = "Latitude is required")
  @Min(value = -90, message = "Latitude must be between -90 and 90")
  @Max(value = 90, message = "Latitude must be between -90 and 90")
  private Double latitude;

  @NotNull(message = "Longitude is required")
  @Min(value = -180, message = "Longitude must be between -180 and 180")
  @Max(value = 180, message = "Longitude must be between -180 and 180")
  private Double longitude;

  @NotNull(message = "Altitude is required")
  @Min(value = 0, message = "Altitude cannot be negative")
  @Max(value = 1000, message = "Altitude cannot exceed 1000m")
  private Double altitude;

  private Integer gpsSatellites;
  private Double gpsHdop;

  @Min(value = 0, message = "Heading must be between 0 and 360")
  @Max(value = 360, message = "Heading must be between 0 and 360")
  private Double heading;

  @Min(value = -90, message = "Pitch must be between -90 and 90")
  @Max(value = 90, message = "Pitch must be between -90 and 90")
  private Double pitch;

  @Min(value = -180, message = "Roll must be between -180 and 180")
  @Max(value = 180, message = "Roll must be between -180 and 180")
  private Double roll;

  @Min(value = 0, message = "Ground speed cannot be negative")
  private Double groundSpeed;

  private Double verticalSpeed;

  @Min(value = 0, message = "Battery voltage cannot be negative")
  private Double batteryVoltage;

  private Double batteryCurrent;

  @NotNull(message = "Battery level is required")
  @Min(value = 0, message = "Battery level must be between 0 and 100")
  @Max(value = 100, message = "Battery level must be between 0 and 100")
  private Integer batteryLevel;

  private Double batteryTemperature;

  private String motorRpm; // JSON string containing motor RPM data

  @Min(value = 0, message = "Signal strength must be between 0 and 100")
  @Max(value = 100, message = "Signal strength must be between 0 and 100")
  private Integer signalStrength;
}
