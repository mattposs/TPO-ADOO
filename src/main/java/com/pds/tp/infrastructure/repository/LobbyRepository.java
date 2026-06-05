package com.pds.tp.infrastructure.repository;

import com.pds.tp.domain.entity.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LobbyRepository extends JpaRepository<Lobby, UUID> {
    List<Lobby> findAllByRegionAndMinRankLessThanEqualAndMaxRankGreaterThanEqualAndMaxPingLessThanEqualAndStatusEquals(String region,
                                                                                                                       String minRank,
                                                                                                                       String maxRank,
                                                                                                                       int maxPing,
                                                                                                                       String status);

    List<Lobby> findAllByStatusEquals(String status);

    List<Lobby> findAllByStatusEqualsAndScheduledTimeLessThanEqual(String status, LocalDateTime scheduledTime);
}


