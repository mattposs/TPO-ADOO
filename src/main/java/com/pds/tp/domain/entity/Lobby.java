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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private LocalDateTime scheduledTime;
    private int maxPlayers;
    private int minPlayers;
    private String region;
    private String minRank;
    private String maxRank;
    private int maxPing;
    private String gameMode;
    private String map;
    @Setter
    private String status;
    @OneToOne
    private Player host;
    @OneToMany
    private List<Player> players;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> confirmedPlayerUsernames;

    public Lobby(LocalDateTime scheduledTime, int maxPlayers, int minPlayers, String region, String minRank, String maxRank, int maxPing, String gameMode, String map, String status, Player host, List<Player> players, Set<String> confirmedPlayerUsernames) {
        this.scheduledTime = scheduledTime;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
        this.region = region;
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.maxPing = maxPing;
        this.gameMode = gameMode;
        this.map = map;
        this.status = status;
        this.host = host;
        this.players = players;
        this.confirmedPlayerUsernames = confirmedPlayerUsernames;
    }

    public Set<String> getConfirmedPlayerUsernames() {
        if (confirmedPlayerUsernames == null) {
            confirmedPlayerUsernames = new HashSet<>();
        }
        return confirmedPlayerUsernames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Lobby lobby = (Lobby) o;

        return new EqualsBuilder().append(id, lobby.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("scheduledTime", scheduledTime)
                .append("maxPlayers", maxPlayers)
                .append("minPlayers", minPlayers)
                .append("region", region)
                .append("minRank", minRank)
                .append("maxRank", maxRank)
                .append("maxPing", maxPing)
                .append("gameMode", gameMode)
                .append("status", status)
                .append("host", host)
                .append("players", players)
                .toString();
    }
}


