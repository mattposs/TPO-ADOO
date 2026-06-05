package com.pds.tp.application.service;

import com.pds.tp.application.dto.*;
import com.pds.tp.domain.builder.ScrimBuilder;
import com.pds.tp.domain.entity.Lobby;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.domain.entity.Scrim;
import com.pds.tp.domain.entity.ScrimStatistics;
import com.pds.tp.domain.entity.Waitlist;
import com.pds.tp.domain.entity.WaitlistStatus;
import com.pds.tp.domain.event.ScrimCreatedEvent;
import com.pds.tp.domain.shared.RankScale;
import com.pds.tp.domain.state.ScrimContext;
import com.pds.tp.domain.state.ScrimStateResolver;
import com.pds.tp.domain.strategy.MatchmakingStrategy;
import com.pds.tp.infrastructure.repository.LobbyRepository;
import com.pds.tp.infrastructure.repository.PlayerRepository;
import com.pds.tp.infrastructure.repository.ScrimRepository;
import com.pds.tp.infrastructure.repository.ScrimStatisticsRepository;
import com.pds.tp.infrastructure.repository.WaitlistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ScrimService {
    private static final String STATUS_BUSCANDO = "Buscando";
    private static final String STATUS_CONFIRMADO = "Confirmado";
    private static final String STATUS_EN_JUEGO = "EnJuego";
    private static final String STATUS_FINALIZADO = "Finalizado";

    private final ScrimRepository scrimRepository;
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    private final ScrimStatisticsRepository scrimStatisticsRepository;
    private final WaitlistRepository waitlistRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final MatchmakingStrategy matchmakingStrategy;
    private final ScrimStateResolver stateResolver;

    public ScrimService(ScrimRepository scrimRepository, LobbyRepository lobbyRepository,
                        PlayerRepository playerRepository, ScrimStatisticsRepository scrimStatisticsRepository,
                        WaitlistRepository waitlistRepository,
                        ApplicationEventPublisher eventPublisher,
                        MatchmakingStrategy matchmakingStrategy,
                        ScrimStateResolver stateResolver) {
        this.scrimRepository = scrimRepository;
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
        this.scrimStatisticsRepository = scrimStatisticsRepository;
        this.waitlistRepository = waitlistRepository;
        this.eventPublisher = eventPublisher;
        this.matchmakingStrategy = matchmakingStrategy;
        this.stateResolver = stateResolver;
    }

    public Lobby createLobby(LobbyData lobbyData) {
        Player player = playerRepository.findByUsername(lobbyData.hostUserName());

        Lobby lobby = new ScrimBuilder()
                .host(player)
                .formato(lobbyData.minPlayers(), lobbyData.maxPlayers())
                .rango(lobbyData.minRank(), lobbyData.maxRank())
                .juego(lobbyData.gameMode(), lobbyData.map())
                .latenciaMax(lobbyData.maxPing())
                .build();

        Lobby savedLobby = lobbyRepository.save(lobby);
        eventPublisher.publishEvent(new ScrimCreatedEvent(this, savedLobby.getId(), savedLobby.getGameMode(), savedLobby.getRegion()));
        return savedLobby;
    }

    public Lobby createScrim(CreateScrimRequest request) {
        if (request.playersPerSide() <= 0 || request.playersPerSide() > 5) {
            throw new IllegalArgumentException("Players per side must be between 1 and 5.");
        }

        int minPlayers = request.playersPerSide();
        int maxPlayers = request.totalPlayers() > 0
                ? request.totalPlayers()
                : request.playersPerSide() * 2;

        if (maxPlayers < 2 || maxPlayers > 10) {
            throw new IllegalArgumentException("Total player count must be between 2 and 10.");
        }

        Player host = playerRepository.findByUsername(request.hostUserName());
        LocalDateTime scheduledTime = parseScheduledDate(request.scheduledDate());

        Lobby lobby = new ScrimBuilder()
                .host(host)
                .fecha(scheduledTime)
                .region(request.region())
                .formato(minPlayers, maxPlayers)
                .rango(request.minRank(), request.maxRank())
                .juego(request.game() != null ? request.game() : request.format(), request.map())
                .latenciaMax(request.maxLatency())
                .build();

        return lobbyRepository.save(lobby);
    }

    public LobbyConfirmation applyToLobby(LobbyApplication lobbyApplication) {
        Lobby lobby = lobbyRepository.getReferenceById(UUID.fromString(lobbyApplication.lobbyId()));
        Player player = playerRepository.findByUsername(lobbyApplication.username());

        if (lobby.getPlayers().size() >= lobby.getMaxPlayers() && !lobby.getPlayers().contains(player)) {
            enqueueWaitlistIfNeeded(lobby, player, lobbyApplication.desiredRole());
            return new LobbyConfirmation(
                    player.getId().toString(),
                    lobby.getId().toString(),
                    "Waitlisted",
                    "Lobby is full. Player added to waitlist."
            );
        }

        ScrimContext context = new ScrimContext(lobby, stateResolver.resolve(lobby.getStatus()), eventPublisher);
        try {
            context.postular(player, lobbyApplication.desiredRole());
            lobbyRepository.save(lobby);
            return new LobbyConfirmation(player.getId().toString(), lobby.getId().toString(), "Confirmed", "Joined lobby successfully.");
        } catch (IllegalStateException e) {
            log.info("Application rejected: {}", e.getMessage());
            return new LobbyConfirmation(lobbyApplication.username(), lobbyApplication.lobbyId(), "Rejected", e.getMessage());
        }
    }

    public String confirmParticipation(UUID lobbyId, String username) {
        Lobby lobby = lobbyRepository.getReferenceById(lobbyId);
        Player player = playerRepository.findByUsername(username);

        ScrimContext context = new ScrimContext(lobby, stateResolver.resolve(lobby.getStatus()), eventPublisher);
        try {
            context.confirmar(player);
            lobbyRepository.save(lobby);
            return "Player confirmed successfully.";
        } catch (IllegalStateException e) {
            return "Confirmation error: " + e.getMessage();
        }
    }

    @Deprecated
    public String confirmarParticipacion(UUID lobbyId, String username) {
        return confirmParticipation(lobbyId, username);
    }

    public Scrim startScrim(ScrimData scrimData) {
        Lobby lobby = lobbyRepository.getReferenceById(UUID.fromString(scrimData.lobbyId()));

        return scrimRepository.findByLobbyId(lobby)
                .orElseGet(() -> createAndPersistScrim(lobby));
    }

    private Scrim createAndPersistScrim(Lobby lobby) {
        ScrimContext context = new ScrimContext(lobby, stateResolver.resolve(lobby.getStatus()), eventPublisher);

        // State transition validation is delegated to the current ScrimState.
        context.iniciar();
        lobbyRepository.save(lobby);

        Scrim scrim = scrimRepository.save(new Scrim(lobby, lobby.getGameMode(), lobby.getMap(), context.getState().getStatusName()));

        List<Player> teamRed = lobby.getPlayers().subList(0, lobby.getPlayers().size() / 2);
        List<Player> teamBlue = lobby.getPlayers().subList(lobby.getPlayers().size() / 2, lobby.getPlayers().size());
        ScrimStatistics stats = new ScrimStatistics(scrim, teamRed, teamBlue);
        scrimStatisticsRepository.save(stats);

        return scrim;
    }

    public int autoStartConfirmedLobbies(LocalDateTime now) {
        List<Lobby> lobbiesToStart = lobbyRepository
                .findAllByStatusEqualsAndScheduledTimeLessThanEqual(STATUS_CONFIRMADO, now);

        int started = 0;
        for (Lobby lobby : lobbiesToStart) {
            if (scrimRepository.findByLobbyId(lobby).isPresent()) {
                continue;
            }

            try {
                createAndPersistScrim(lobby);
                started++;
            } catch (IllegalStateException ex) {
                log.warn("Scheduler could not start lobby {}: {}", lobby.getId(), ex.getMessage());
            }
        }

        return started;
    }

    public int autoFinalizeRunningScrims(LocalDateTime now, long maxDurationHours) {
        List<Scrim> runningScrims = scrimRepository.findAllByStatusEquals(STATUS_EN_JUEGO);
        int finalized = 0;

        for (Scrim scrim : runningScrims) {
            if (scrim.getStartTime() == null) {
                continue;
            }

            long runningHours = ChronoUnit.HOURS.between(scrim.getStartTime(), now);
            if (runningHours < maxDurationHours) {
                continue;
            }

            try {
                finishScrimById(scrim.getId());
                finalized++;
            } catch (IllegalStateException ex) {
                log.warn("Scheduler could not finish scrim {}: {}", scrim.getId(), ex.getMessage());
            }
        }

        return finalized;
    }

    public String cancelLobbyById(UUID lobbyId) {
        Lobby lobby = lobbyRepository.getReferenceById(lobbyId);
        ScrimContext context = new ScrimContext(lobby, stateResolver.resolve(lobby.getStatus()), eventPublisher);

        try {
            context.cancelar();
            lobbyRepository.save(lobby);
            return "Lobby " + lobbyId + " canceled.";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    public String finishScrimById(UUID scrimId) {
        Scrim scrim = scrimRepository.getReferenceById(scrimId);
        Lobby lobby = scrim.getLobbyId();
        ScrimContext context = new ScrimContext(lobby, stateResolver.resolve(lobby.getStatus()), eventPublisher);

        try {
            context.finalizar();
            lobbyRepository.save(lobby);

            scrim.setStatus(context.getState().getStatusName());
            scrimRepository.save(scrim);

            ScrimStatistics stats = scrimStatisticsRepository.findByScrimId(scrim);
            stats.setEndTime(LocalDateTime.now());
            stats.setStatus(STATUS_FINALIZADO);
            scrimStatisticsRepository.save(stats);

            return "Scrim finished successfully.";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    public List<Lobby> findActiveLobbiesByRegionAndRank(FindLobbyData data) {
        return lobbyRepository.findAllByStatusEquals(STATUS_BUSCANDO)
                .stream()
                .filter(lobby -> data.game() == null || data.game().isBlank() || lobby.getGameMode().equalsIgnoreCase(data.game()))
                .filter(lobby -> data.region() == null || data.region().isBlank() || lobby.getRegion().equalsIgnoreCase(data.region()))
                .filter(lobby -> data.minRank() == null || data.minRank().isBlank() || compareRanks(lobby.getMinRank(), data.minRank()) <= 0)
                .filter(lobby -> data.maxRank() == null || data.maxRank().isBlank() || compareRanks(lobby.getMaxRank(), data.maxRank()) >= 0)
                .filter(lobby -> data.maxLatency() == null || lobby.getMaxPing() <= data.maxLatency())
                .filter(lobby -> data.date() == null || data.date().isBlank() || isSameDay(lobby, data.date()))
                .toList();
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void autoMatchmakingCron() {
        List<Lobby> lobbies = lobbyRepository.findAllByStatusEquals(STATUS_BUSCANDO);
        // Use the full player pool as matchmaking candidates for this scheduled prototype flow.
        List<Player> availablePlayers = playerRepository.findAll();

        for (Lobby lobby : lobbies) {
            promoteWaitlistedPlayers(lobby);

            List<Player> selected = matchmakingStrategy.seleccionar(availablePlayers, lobby);

            ScrimContext context = new ScrimContext(lobby, stateResolver.resolve(lobby.getStatus()), eventPublisher);
            for (Player p : selected) {
                try {
                    context.postular(p, "FLEX");
                } catch (IllegalStateException ignored) {
                    // A selected player can become invalid while the lobby is being filled.
                }
            }
            lobbyRepository.save(lobby);
        }
    }

    private void enqueueWaitlistIfNeeded(Lobby lobby, Player player, String desiredRole) {
        waitlistRepository.findFirstByLobbyAndPlayerAndStatus(lobby, player, WaitlistStatus.PENDIENTE)
                .orElseGet(() -> waitlistRepository.save(new Waitlist(lobby, player, desiredRole)));
    }

    private void promoteWaitlistedPlayers(Lobby lobby) {
        int availableSlots = lobby.getMaxPlayers() - lobby.getPlayers().size();
        if (availableSlots <= 0) {
            return;
        }

        List<Waitlist> pendingEntries = waitlistRepository.findAllByLobbyAndStatusOrderByCreatedAtAsc(lobby, WaitlistStatus.PENDIENTE);
        if (pendingEntries.isEmpty()) {
            return;
        }

        ScrimContext context = new ScrimContext(lobby, stateResolver.resolve(lobby.getStatus()), eventPublisher);
        for (Waitlist entry : pendingEntries) {
            if (availableSlots <= 0) {
                break;
            }

            try {
                context.postular(entry.getPlayer(), entry.getDesiredRole() != null ? entry.getDesiredRole() : "FLEX");
                entry.setStatus(WaitlistStatus.PROMOVIDO);
                entry.setPromotedAt(LocalDateTime.now());
                waitlistRepository.save(entry);
                availableSlots--;
            } catch (IllegalStateException ignored) {
                // Keep entry pending if current state/rules still block promotion.
            }
        }
    }

    public ScrimStatistics getStatistics(UUID scrimId) {
        return scrimStatisticsRepository.findByScrimId(scrimRepository.getReferenceById(scrimId));
    }

    public ScrimStatistics saveStatistics(UUID scrimId, CreateStatisticsRequest request) {
        Scrim scrim = scrimRepository.getReferenceById(scrimId);
        ScrimStatistics statistics = scrimStatisticsRepository.findByScrimId(scrim);

        if (request.winningTeam() != null && !request.winningTeam().isBlank()) {
            statistics.setWinningTeam(request.winningTeam());
        }
        if (request.status() != null && !request.status().isBlank()) {
            statistics.setStatus(request.status());
        }

        return scrimStatisticsRepository.save(statistics);
    }

    private int compareRanks(String a, String b) {
        return RankScale.toValue(a) - RankScale.toValue(b);
    }

    private boolean isSameDay(Lobby lobby, String fecha) {
        try {
            LocalDateTime requested = LocalDateTime.parse(fecha);
            return lobby.getScheduledTime() != null && lobby.getScheduledTime().toLocalDate().isEqual(requested.toLocalDate());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format in filter. Use ISO-8601, for example 2026-06-18T21:00:00.");
        }
    }

    private LocalDateTime parseScheduledDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(rawDate);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use ISO-8601, for example 2026-06-18T21:00:00.");
        }
    }
}

