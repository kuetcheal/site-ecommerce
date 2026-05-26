package com.kuetche.siteecommerce.controller;

import com.kuetche.siteecommerce.dto.CommandeResponse;
import com.kuetche.siteecommerce.dto.CreateCommandeRequest;
import com.kuetche.siteecommerce.dto.StripeCheckoutResponse;
import com.kuetche.siteecommerce.service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    @PostMapping("/payer")
    public ResponseEntity<StripeCheckoutResponse> creerCommandeEtPayer(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCommandeRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commandeService.creerCommandeEtSessionPaiement(jwt, request));
    }

    @GetMapping("/mes-commandes")
    public ResponseEntity<List<CommandeResponse>> mesCommandes(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(commandeService.mesCommandes(jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeResponse> detailCommande(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(commandeService.detailCommande(jwt, id));
    }
}