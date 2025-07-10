package com.flytbase.drone.controller;

import com.flytbase.drone.dto.telemetry.TelemetryRequest;
import com.flytbase.drone.dto.telemetry.TelemetryResponse;
import com.flytbase.drone.service.TelemetryService;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for drone telemetry operations. */
@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin
@RequiredArgsConstructor
public class TelemetryController {

  private final TelemetryService telemetryService;

  /**
   * Record telemetry data for a mission. This would typically be called by the drone or ground
   * control system.
   */
  @PostMapping("/missions/{missionId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<TelemetryResponse> recordTelemetry(
      @PathVariable UUID missionId, @Valid @RequestBody TelemetryRequest request) {
    TelemetryResponse response = telemetryService.recordTelemetry(missionId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Get telemetry history for a mission. */
  @GetMapping("/missions/{missionId}/history")
  public ResponseEntity<Page<TelemetryResponse>> getTelemetryHistory(
      @PathVariable UUID missionId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
      Pageable pageable) {
    Page<TelemetryResponse> history =
        telemetryService.getTelemetryHistory(missionId, startTime, endTime, pageable);
    return ResponseEntity.ok(history);
  }

  /** Get latest telemetry for a mission. */
  @GetMapping("/missions/{missionId}/latest")
  public ResponseEntity<TelemetryResponse> getLatestTelemetry(@PathVariable UUID missionId) {
    TelemetryResponse latest = telemetryService.getLatestTelemetry(missionId);
    return ResponseEntity.ok(latest);
  }

  /**
   * Simulate telemetry for testing (development only). In production, this would be removed and
   * telemetry would come from actual drones.
   */
  @PostMapping("/missions/{missionId}/simulate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> simulateTelemetry(
      @PathVariable UUID missionId, @RequestParam(defaultValue = "60") int durationSeconds) {

    // This starts a background thread to simulate telemetry
    new Thread(
            () -> {
              try {
                double baseLat = 37.7749;
                double baseLon = -122.4194;

                for (int i = 0; i < durationSeconds; i++) {
                  TelemetryRequest telemetry = new TelemetryRequest();
                  telemetry.setLatitude(baseLat + (i * 0.0001));
                  telemetry.setLongitude(baseLon + (i * 0.0001));
                  telemetry.setAltitude(100.0 + Math.random() * 20);
                  telemetry.setGpsSatellites(12 + (int) (Math.random() * 4));
                  telemetry.setGpsHdop(0.8 + Math.random() * 0.4);
                  telemetry.setHeading(Math.random() * 360);
                  telemetry.setPitch(-5 + Math.random() * 10);
                  telemetry.setRoll(-5 + Math.random() * 10);
                  telemetry.setGroundSpeed(5.0 + Math.random() * 10);
                  telemetry.setVerticalSpeed(-2 + Math.random() * 4);
                  telemetry.setBatteryVoltage(22.0 + Math.random() * 2);
                  telemetry.setBatteryCurrent(10.0 + Math.random() * 5);
                  telemetry.setBatteryLevel(100 - (i * 100 / durationSeconds));
                  telemetry.setBatteryTemperature(25.0 + Math.random() * 10);
                  telemetry.setSignalStrength(80 + (int) (Math.random() * 20));

                  telemetryService.recordTelemetry(missionId, telemetry);

                  Thread.sleep(1000); // 1 second intervals
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            })
        .start();

    return ResponseEntity.ok("Telemetry simulation started for " + durationSeconds + " seconds");
  }
}
