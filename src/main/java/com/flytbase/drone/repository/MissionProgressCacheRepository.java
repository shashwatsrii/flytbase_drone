package com.flytbase.drone.repository;

import com.flytbase.drone.entity.MissionProgressCache;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for MissionProgressCache operations. */
@Repository
public interface MissionProgressCacheRepository extends JpaRepository<MissionProgressCache, UUID> {
  Optional<MissionProgressCache> findByMissionId(UUID missionId);
}
