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

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String username;
    private String email;
    private String password;
    @Setter
    @Enumerated(EnumType.STRING)
    private EmailVerificationStatus emailVerificationStatus;
    @Setter
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String preferredRole;
    private String region;
    private String platform;
    private String availability;
    @Setter
    private String visibleRank;
    @Setter
    private int averagePingMs;
    private int rank;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private double kda;

    public Player(String username, String email, String password, String preferredRole, String region, String platform, String availability) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.emailVerificationStatus = EmailVerificationStatus.PENDIENTE;
        this.role = UserRole.USER;
        this.preferredRole = preferredRole;
        this.region = region;
        this.platform = platform;
        this.availability = availability;
        this.visibleRank = "BRONCE";
        this.averagePingMs = 60;
        this.rank = 0;
        this.gamesPlayed = 0;
        this.wins = 0;
        this.losses = 0;
        this.kda = 0.0;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return new EqualsBuilder().append(id, player.id).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("username", username)
                .append("email", email)
                .append("password", password)
                .append("emailVerificationStatus", emailVerificationStatus)
                .append("role", role)
                .append("preferredRole", preferredRole)
                .append("region", region)
                .append("platform", platform)
                .append("availability", availability)
                .append("visibleRank", visibleRank)
                .append("averagePingMs", averagePingMs)
                .append("rank", rank)
                .append("gamesPlayed", gamesPlayed)
                .append("wins", wins)
                .append("losses", losses)
                .append("kda", kda)
                .toString();
    }
}


