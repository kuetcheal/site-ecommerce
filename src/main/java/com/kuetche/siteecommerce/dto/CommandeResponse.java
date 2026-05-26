package com.kuetche.siteecommerce.dto;

import com.kuetche.siteecommerce.entity.Commande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CommandeResponse(
        Long id,
        Long clientId,
        BigDecimal montantTotal,
        String statut,
        String statutPaiement,
        String stripeSessionId,
        LocalDateTime createdAt,
        List<LigneCommandeResponse> lignes
) {
    public static CommandeResponse from(Commande commande) {
        return new CommandeResponse(
                commande.getId(),
                commande.getClient().getId(),
                commande.getMontantTotal(),
                commande.getStatut().name(),
                commande.getStatutPaiement().name(),
                commande.getStripeSessionId(),
                commande.getCreatedAt(),
                commande.getLignes()
                        .stream()
                        .map(LigneCommandeResponse::from)
                        .toList()
        );
    }
}