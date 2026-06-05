package com.pds.tp.infrastructure.repository;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Scrim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScrimRepository extends JpaRepository<Scrim, UUID> {
    Optional<Scrim> findByLobbyId(Lobby lobby);

    List<Scrim> findAllByStatusEquals(String status);
}


