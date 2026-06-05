package com.pds.tp.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Scrim {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne
    private Lobby lobbyId;
    private String gameMode;
    private String map;
    private LocalDateTime startTime;
    @Setter
    private LocalDateTime endTime;
    @Setter
    private String status;

    public Scrim(Lobby lobby, String gameMode, String map, String status) {
        this.lobbyId = lobby;
        this.gameMode = gameMode;
        this.map = map;
        this.startTime = LocalDateTime.now();
        this.endTime = null;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Scrim scrim = (Scrim) o;

        return new EqualsBuilder().append(id, scrim.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("lobbyId", lobbyId)
                .append("gameMode", gameMode)
                .append("map", map)
                .append("startTime", startTime)
                .append("endTime", endTime)
                .append("status", status)
                .toString();
    }

}


