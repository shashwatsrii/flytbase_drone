package com.flytbase.drone.service;

import com.flytbase.drone.dto.drone.CreateDroneRequest;
import com.flytbase.drone.dto.drone.DroneResponse;
import com.flytbase.drone.dto.drone.UpdateDroneRequest;
import com.flytbase.drone.entity.Drone;
import com.flytbase.drone.entity.Organization;
import com.flytbase.drone.entity.User;
import com.flytbase.drone.exception.BusinessException;
import com.flytbase.drone.repository.DroneRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for drone operations. */
@Service
@RequiredArgsConstructor
@Transactional
public class DroneService {

  private final DroneRepository droneRepository;
  private final UserService userService;

  /**
   * Get all drones for the current user's organization.
   *
   * @param pageable pagination information
   * @return page of drone responses
   */
  @Transactional(readOnly = true)
  public Page<DroneResponse> getAllDrones(Pageable pageable) {
    User currentUser = userService.getCurrentUser();
    return droneRepository
        .findByOrganizationId(currentUser.getOrganization().getId(), pageable)
        .map(DroneResponse::fromEntity);
  }

  /**
   * Get a drone by ID.
   *
   * @param id the drone ID
   * @return the drone response
   */
  @Transactional(readOnly = true)
  public DroneResponse getDroneById(UUID id) {
    User currentUser = userService.getCurrentUser();
    Drone drone =
        droneRepository
            .findByIdAndOrganizationId(id, currentUser.getOrganization().getId())
            .orElseThrow(() -> new BusinessException("Drone not found with ID: " + id));
    return DroneResponse.fromEntity(drone);
  }

  /**
   * Create a new drone.
   *
   * @param request the create drone request
   * @return the created drone response
   */
  public DroneResponse createDrone(CreateDroneRequest request) {
    User currentUser = userService.getCurrentUser();
    Organization organization = currentUser.getOrganization();

    // Check if serial number already exists
    if (droneRepository.existsBySerialNumber(request.getSerialNumber())) {
      throw new BusinessException(
          "Drone with serial number " + request.getSerialNumber() + " already exists");
    }

    Drone drone = new Drone();
    drone.setName(request.getName());
    drone.setModel(request.getModel());
    drone.setSerialNumber(request.getSerialNumber());
    drone.setStatus(request.getStatus());
    drone.setBatteryLevel(request.getBatteryLevel());
    drone.setLastMaintenanceDate(request.getLastMaintenanceDate());
    drone.setTotalFlightHours(request.getTotalFlightHours());
    drone.setHomeLocationLatitude(request.getHomeLocation().getLatitude());
    drone.setHomeLocationLongitude(request.getHomeLocation().getLongitude());
    drone.setOrganization(organization);
    drone.setCreatedBy(currentUser);

    drone = droneRepository.save(drone);
    return DroneResponse.fromEntity(drone);
  }

  /**
   * Update an existing drone.
   *
   * @param id the drone ID
   * @param request the update drone request
   * @return the updated drone response
   */
  public DroneResponse updateDrone(UUID id, UpdateDroneRequest request) {
    User currentUser = userService.getCurrentUser();
    Drone drone =
        droneRepository
            .findByIdAndOrganizationId(id, currentUser.getOrganization().getId())
            .orElseThrow(() -> new BusinessException("Drone not found with ID: " + id));

    if (request.getName() != null) {
      drone.setName(request.getName());
    }
    if (request.getModel() != null) {
      drone.setModel(request.getModel());
    }
    if (request.getStatus() != null) {
      drone.setStatus(request.getStatus());
    }
    if (request.getBatteryLevel() != null) {
      drone.setBatteryLevel(request.getBatteryLevel());
    }
    if (request.getLastMaintenanceDate() != null) {
      drone.setLastMaintenanceDate(request.getLastMaintenanceDate());
    }
    if (request.getTotalFlightHours() != null) {
      drone.setTotalFlightHours(request.getTotalFlightHours());
    }

    drone = droneRepository.save(drone);
    return DroneResponse.fromEntity(drone);
  }

  /**
   * Delete a drone.
   *
   * @param id the drone ID
   */
  public void deleteDrone(UUID id) {
    User currentUser = userService.getCurrentUser();
    Drone drone =
        droneRepository
            .findByIdAndOrganizationId(id, currentUser.getOrganization().getId())
            .orElseThrow(() -> new BusinessException("Drone not found with ID: " + id));

    droneRepository.delete(drone);
  }

  /**
   * Get drones by status.
   *
   * @param status the drone status
   * @return list of drone responses
   */
  @Transactional(readOnly = true)
  public List<DroneResponse> getDronesByStatus(Drone.DroneStatus status) {
    User currentUser = userService.getCurrentUser();
    return droneRepository
        .findByOrganizationIdAndStatus(currentUser.getOrganization().getId(), status)
        .stream()
        .map(DroneResponse::fromEntity)
        .collect(Collectors.toList());
  }
}
