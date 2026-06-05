package com.pds.tp.infrastructure.repository;

import com.pds.tp.domain.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    boolean existsByUsernameAndPassword(String username, String password);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Player findByUsername(String username);

    Player findByEmail(String email);

}


