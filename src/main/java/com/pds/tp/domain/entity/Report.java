package com.pds.tp.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    private Scrim scrimId;
    @OneToOne
    private Player reportingPlayer;
    @ManyToOne
    private Player reportedPlayer;
    private String reason;
    private String description;
    private String status;
    private String reportedAt;
    private String resolvedAt;
    private String resolutionDetails;

    public Report(Scrim scrimId, Player reportingPlayer, Player reportedPlayer, String reason, String description) {
        this.scrimId = scrimId;
        this.reportingPlayer = reportingPlayer;
        this.reportedPlayer = reportedPlayer;
        this.reason = reason;
        this.description = description;
        this.status = "Created";
        this.reportedAt = LocalDate.now().toString();
        this.resolvedAt = null;
        this.resolutionDetails = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Report report = (Report) o;

        return new EqualsBuilder().append(id, report.id).isEquals();
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
                .append("playerName", reportingPlayer)
                .append("reason", reason)
                .append("description", description)
                .append("status", status)
                .append("reportedAt", reportedAt)
                .append("resolvedAt", resolvedAt)
                .append("resolutionDetails", resolutionDetails)
                .toString();
    }
}


