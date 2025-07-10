package com.flytbase.drone.repository;

import com.flytbase.drone.entity.DroneTelemetry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for drone telemetry data operations. */
@Repository
public interface DroneTelemetryRepository extends JpaRepository<DroneTelemetry, UUID> {

  /** Find telemetry data for a specific mission within a time range. */
  Page<DroneTelemetry> findByMissionIdAndTimestampBetweenOrderByTimestampDesc(
      UUID missionId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /** Find latest telemetry for a mission. */
  DroneTelemetry findTopByMissionIdOrderByTimestampDesc(UUID missionId);

  /** Find telemetry data for a specific drone within a time range. */
  List<DroneTelemetry> findByDroneIdAndTimestampBetween(
      UUID droneId, LocalDateTime startTime, LocalDateTime endTime);

  /** Get average metrics for a mission. */
  @Query(
      "SELECT AVG(t.groundSpeed) as avgSpeed, AVG(t.gpsAltitude) as avgAltitude, "
          + "MIN(t.batteryLevel) as minBattery, MAX(t.batteryLevel) as maxBattery "
          + "FROM DroneTelemetry t WHERE t.mission.id = :missionId")
  Object[] getMissionAverageMetrics(@Param("missionId") UUID missionId);

  /** Delete old telemetry data. */
  void deleteByTimestampBefore(LocalDateTime cutoffTime);

  /** Count telemetry records for a mission. */
  long countByMissionId(UUID missionId);
}
