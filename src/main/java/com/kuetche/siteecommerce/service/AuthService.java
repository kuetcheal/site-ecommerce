package com.kuetche.siteecommerce.service;

import com.kuetche.siteecommerce.dto.AuthResponse;
import com.kuetche.siteecommerce.dto.ClientResponse;
import com.kuetche.siteecommerce.dto.LoginRequest;
import com.kuetche.siteecommerce.dto.RegisterRequest;
import com.kuetche.siteecommerce.entity.Client;
import com.kuetche.siteecommerce.enums.Role;
import com.kuetche.siteecommerce.repository.ClientRepository;
import com.kuetche.siteecommerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProviderManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (clientRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }

        Client client = Client.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .tel(request.tel())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .role(Role.CLIENT)
                .active(true)
                .build();

        Client savedClient = clientRepository.save(client);

        try {
            emailService.envoyerEmailBienvenue(savedClient);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'e-mail de bienvenue : " + e.getMessage());
        }

        String token = jwtService.generateToken(savedClient);

        return new AuthResponse(
                token,
                "Bearer",
                ClientResponse.from(savedClient)
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.motDePasse()
                )
        );

        Client client = clientRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));

        String token = jwtService.generateToken(client);

        return new AuthResponse(
                token,
                "Bearer",
                ClientResponse.from(client)
        );
    }
}