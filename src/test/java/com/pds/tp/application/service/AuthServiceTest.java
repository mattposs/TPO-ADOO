package com.pds.tp.application.service;

import com.pds.tp.application.dto.PlayerData;
import com.pds.tp.domain.entity.EmailVerificationStatus;
import com.pds.tp.domain.entity.Player;
import com.pds.tp.infrastructure.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.pds.tp.support.TestFixtures.player;
import static com.pds.tp.support.TestFixtures.setField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterWithEncodedPassword() {
        PlayerData input = new PlayerData("neo", "neo@test.com", "plain", "FLEX", "LAS", "PC", "NOCHE");

        when(playerRepository.existsByUsername("neo")).thenReturn(false);
        when(playerRepository.existsByEmail("neo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player saved = authService.register(input);

        assertEquals("neo", saved.getUsername());
        assertEquals("hashed", saved.getPassword());
    }

    @Test
    void shouldRejectWhenUsernameAlreadyExists() {
        PlayerData input = new PlayerData("neo", "neo@test.com", "plain", "FLEX", "LAS", "PC", "NOCHE");
        when(playerRepository.existsByUsername("neo")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(input));
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void shouldAuthenticateByEmailWhenVerified() {
        Player verifiedPlayer = player("neo", "neo@test.com", "LAS");
        verifiedPlayer.setEmailVerificationStatus(EmailVerificationStatus.VERIFICADO);
        setField(verifiedPlayer, "password", "hashed");

        when(playerRepository.findByEmail("neo@test.com")).thenReturn(verifiedPlayer);
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);

        assertTrue(authService.authenticate("neo@test.com", "plain"));
    }

    @Test
    void shouldFailAuthenticationWhenEmailNotVerified() {
        Player pendingPlayer = player("neo", "neo@test.com", "LAS");
        pendingPlayer.setEmailVerificationStatus(EmailVerificationStatus.PENDIENTE);
        setField(pendingPlayer, "password", "hashed");

        when(playerRepository.findByUsername("neo")).thenReturn(pendingPlayer);

        assertFalse(authService.authenticate("neo", "plain"));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void shouldVerifyEmail() {
        Player pendingPlayer = player("neo", "neo@test.com", "LAS");
        pendingPlayer.setEmailVerificationStatus(EmailVerificationStatus.PENDIENTE);
        setField(pendingPlayer, "password", "hashed");

        when(playerRepository.findByUsername("neo")).thenReturn(pendingPlayer);

        String result = authService.verifyEmail("neo");

        assertEquals("Email verified successfully.", result);
        assertEquals(EmailVerificationStatus.VERIFICADO, pendingPlayer.getEmailVerificationStatus());
        verify(playerRepository).save(pendingPlayer);
    }
}

