package com.pds.tp.infrastructure.repository;

import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.entity.Waitlist;
import com.pds.tp.domain.entity.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaitlistRepository extends JpaRepository<Waitlist, UUID> {
    Optional<Waitlist> findFirstByLobbyAndPlayerAndStatus(Lobby lobby, Player player, WaitlistStatus status);

    List<Waitlist> findAllByLobbyAndStatusOrderByCreatedAtAsc(Lobby lobby, WaitlistStatus status);
}

