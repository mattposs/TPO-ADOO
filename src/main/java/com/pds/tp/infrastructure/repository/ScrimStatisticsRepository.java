package com.pds.tp.infrastructure.repository;

import com.pds.tp.domain.entity.Scrim;
import com.pds.tp.domain.entity.ScrimStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScrimStatisticsRepository extends JpaRepository<ScrimStatistics, UUID> {
    ScrimStatistics findByScrimId(Scrim scrim);
}


