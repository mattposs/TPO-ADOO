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
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String type;
    private String payload;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Setter
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;
}


