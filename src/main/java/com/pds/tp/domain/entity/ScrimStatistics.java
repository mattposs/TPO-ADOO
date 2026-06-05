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
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class ScrimStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne
    private Scrim scrimId;
    @OneToMany
    private List<Player> redTeam;
    @OneToMany
    private List<Player> blueTeam;
    @Setter
    private String winningTeam;
    @Setter
    private String status;
    private LocalDateTime startTime;
    @Setter
    private LocalDateTime endTime;

    public ScrimStatistics(Scrim scrimId, List<Player> redTeam, List<Player> blueTeam) {
        this.scrimId = scrimId;
        this.redTeam = redTeam;
        this.blueTeam = blueTeam;
        this.winningTeam = null;
        this.status = "Created";
        this.startTime = LocalDateTime.now();
        this.endTime = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ScrimStatistics that = (ScrimStatistics) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("scrimId", scrimId)
                .append("redTeam", redTeam)
                .append("blueTeam", blueTeam)
                .append("winningTeam", winningTeam)
                .toString();
    }
}


