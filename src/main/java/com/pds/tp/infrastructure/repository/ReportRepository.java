package com.pds.tp.infrastructure.repository;

import com.pds.tp.domain.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
}


