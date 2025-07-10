package com.flytbase.drone.controller;

import com.flytbase.drone.dto.drone.CreateDroneRequest;
import com.flytbase.drone.dto.drone.DroneResponse;
import com.flytbase.drone.dto.drone.UpdateDroneRequest;
import com.flytbase.drone.entity.Drone;
import com.flytbase.drone.service.DroneService;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for drone operations. */
@RestController
@RequestMapping("/api/drones")
@CrossOrigin
@RequiredArgsConstructor
public class DroneController {

  private final DroneService droneService;

  /**
   * Get all drones with pagination.
   *
   * @param pageable pagination information
   * @return page of drone responses
   */
  @GetMapping
  public ResponseEntity<Page<DroneResponse>> getAllDrones(Pageable pageable) {
    return ResponseEntity.ok(droneService.getAllDrones(pageable));
  }

  /**
   * Get a drone by ID.
   *
   * @param id the drone ID
   * @return the drone response
   */
  @GetMapping("/{id}")
  public ResponseEntity<DroneResponse> getDroneById(@PathVariable UUID id) {
    return ResponseEntity.ok(droneService.getDroneById(id));
  }

  /**
   * Create a new drone.
   *
   * @param request the create drone request
   * @return the created drone response
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<DroneResponse> createDrone(@Valid @RequestBody CreateDroneRequest request) {
    DroneResponse response = droneService.createDrone(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing drone.
   *
   * @param id the drone ID
   * @param request the update drone request
   * @return the updated drone response
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
  public ResponseEntity<DroneResponse> updateDrone(
      @PathVariable UUID id, @Valid @RequestBody UpdateDroneRequest request) {
    return ResponseEntity.ok(droneService.updateDrone(id, request));
  }

  /**
   * Delete a drone.
   *
   * @param id the drone ID
   * @return no content response
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteDrone(@PathVariable UUID id) {
    droneService.deleteDrone(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get drones by status.
   *
   * @param status the drone status
   * @return list of drone responses
   */
  @GetMapping("/status/{status}")
  public ResponseEntity<List<DroneResponse>> getDronesByStatus(
      @PathVariable Drone.DroneStatus status) {
    return ResponseEntity.ok(droneService.getDronesByStatus(status));
  }
}
