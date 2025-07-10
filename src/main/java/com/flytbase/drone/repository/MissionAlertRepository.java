package com.flytbase.drone.repository;

import com.flytbase.drone.entity.MissionAlert;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for mission alert operations. */
@Repository
public interface MissionAlertRepository extends JpaRepository<MissionAlert, UUID> {

  /** Find alerts by mission ID. */
  List<MissionAlert> findByMissionIdOrderByOccurredAtDesc(UUID missionId);

  /** Find unacknowledged alerts. */
  List<MissionAlert> findByAcknowledgedFalseOrderBySeverityDescOccurredAtDesc();

  /** Find alerts by mission and severity. */
  List<MissionAlert> findByMissionIdAndSeverity(UUID missionId, MissionAlert.Severity severity);

  /** Check if recent alert of same type exists. */
  boolean existsByMissionIdAndAlertTypeAndOccurredAtAfter(
      UUID missionId, MissionAlert.AlertType alertType, LocalDateTime after);

  /** Count unacknowledged critical alerts. */
  long countByMissionIdAndSeverityAndAcknowledgedFalse(
      UUID missionId, MissionAlert.Severity severity);
}
