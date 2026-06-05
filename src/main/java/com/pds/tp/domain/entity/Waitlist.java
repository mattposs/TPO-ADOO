package com.pds.tp.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Waitlist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Lobby lobby;

    @ManyToOne
    private Player player;

    private String desiredRole;

    @Enumerated(EnumType.STRING)
    @Setter
    private WaitlistStatus status;

    private LocalDateTime createdAt;

    @Setter
    private LocalDateTime promotedAt;

    public Waitlist(Lobby lobby, Player player, String desiredRole) {
        this.lobby = lobby;
        this.player = player;
        this.desiredRole = desiredRole;
        this.status = WaitlistStatus.PENDIENTE;
        this.createdAt = LocalDateTime.now();
        this.promotedAt = null;
    }
}

