package com.pds.tp.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class ScrimApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Player user;

    @ManyToOne
    private Scrim scrim;

    private String desiredRole;

    @Setter
    @Enumerated(EnumType.STRING)
    private ScrimApplicationStatus status = ScrimApplicationStatus.PENDING;
}


