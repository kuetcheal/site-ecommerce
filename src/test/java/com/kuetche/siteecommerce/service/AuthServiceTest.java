package com.kuetche.siteecommerce.service;

import com.kuetche.siteecommerce.dto.AuthResponse;
import com.kuetche.siteecommerce.dto.LoginRequest;
import com.kuetche.siteecommerce.dto.RegisterRequest;
import com.kuetche.siteecommerce.entity.Client;
import com.kuetche.siteecommerce.enums.Role;
import com.kuetche.siteecommerce.repository.ClientRepository;
import com.kuetche.siteecommerce.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProviderManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_doitCreerClientEtRetournerToken() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Kuetche",
                "Alex",
                "alex@test.com",
                "0600000000",
                "password123"
        );

        Client savedClient = Client.builder()
                .id(1L)
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .motDePasse("encoded-password")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(clientRepository.existsByEmail("alex@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
        when(jwtService.generateToken(savedClient)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();

        verify(clientRepository, times(1)).existsByEmail("alex@test.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(emailService, times(1)).envoyerEmailBienvenue(savedClient);
        verify(jwtService, times(1)).generateToken(savedClient);
    }

    @Test
    void register_doitLeverExceptionSiEmailExisteDeja() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Kuetche",
                "Alex",
                "alex@test.com",
                "0600000000",
                "password123"
        );

        when(clientRepository.existsByEmail("alex@test.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cet email est déjà utilisé");

        verify(clientRepository, times(1)).existsByEmail("alex@test.com");
        verify(clientRepository, never()).save(any(Client.class));
        verify(jwtService, never()).generateToken(any(Client.class));
    }

    @Test
    void register_doitContinuerMemeSiEmailBienvenueEchoue() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Kuetche",
                "Alex",
                "alex@test.com",
                "0600000000",
                "password123"
        );

        Client savedClient = Client.builder()
                .id(1L)
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .motDePasse("encoded-password")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(clientRepository.existsByEmail("alex@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
        doThrow(new RuntimeException("Erreur SMTP")).when(emailService).envoyerEmailBienvenue(savedClient);
        when(jwtService.generateToken(savedClient)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();

        verify(emailService, times(1)).envoyerEmailBienvenue(savedClient);
        verify(jwtService, times(1)).generateToken(savedClient);
    }

    @Test
    void login_doitAuthentifierClientEtRetournerToken() {
        // Arrange
        LoginRequest request = new LoginRequest(
                "alex@test.com",
                "password123"
        );

        Client client = Client.builder()
                .id(1L)
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .motDePasse("encoded-password")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(clientRepository.findByEmail("alex@test.com")).thenReturn(Optional.of(client));
        when(jwtService.generateToken(client)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(clientRepository, times(1)).findByEmail("alex@test.com");
        verify(jwtService, times(1)).generateToken(client);
    }

    @Test
    void login_doitLeverExceptionSiClientIntrouvableApresAuthentification() {
        // Arrange
        LoginRequest request = new LoginRequest(
                "unknown@test.com",
                "password123"
        );

        when(clientRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Client introuvable");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(clientRepository, times(1)).findByEmail("unknown@test.com");
        verify(jwtService, never()).generateToken(any(Client.class));
    }
}