package com.kuetche.siteecommerce.controller;

import com.kuetche.siteecommerce.dto.ClientResponse;
import com.kuetche.siteecommerce.entity.Client;
import com.kuetche.siteecommerce.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientRepository clientRepository;

    @GetMapping("/me")
    public ClientResponse me(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));

        return ClientResponse.from(client);
    }
}