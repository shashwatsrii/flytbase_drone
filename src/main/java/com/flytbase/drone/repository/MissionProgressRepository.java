package com.flytbase.drone.repository;

import com.flytbase.drone.entity.Mission;
import com.flytbase.drone.entity.MissionProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for managing MissionProgress entities. */
@Repository
public interface MissionProgressRepository extends JpaRepository<MissionProgress, UUID> {

  /**
   * Find the latest progress update for a specific mission.
   *
   * @param mission the mission to find progress for
   * @return the latest progress update
   */
  Optional<MissionProgress> findTopByMissionOrderByTimestampDesc(Mission mission);

  /**
   * Find all progress updates for a specific mission ordered by timestamp.
   *
   * @param mission the mission to find progress for
   * @return list of progress updates
   */
  List<MissionProgress> findByMissionOrderByTimestampAsc(Mission mission);

  /**
   * Find all progress updates for a mission with the given ID.
   *
   * @param missionId the ID of the mission
   * @return list of progress updates
   */
  List<MissionProgress> findByMissionIdOrderByTimestampAsc(UUID missionId);
}
