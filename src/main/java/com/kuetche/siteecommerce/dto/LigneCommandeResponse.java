package com.kuetche.siteecommerce.dto;

import com.kuetche.siteecommerce.entity.LigneCommande;

import java.math.BigDecimal;

public record LigneCommandeResponse(
        Long id,
        Long produitId,
        String nomProduit,
        Integer quantite,
        BigDecimal prixUnitaire,
        BigDecimal sousTotal
) {
    public static LigneCommandeResponse from(LigneCommande ligne) {
        return new LigneCommandeResponse(
                ligne.getId(),
                ligne.getProduit().getId(),
                ligne.getNomProduit(),
                ligne.getQuantite(),
                ligne.getPrixUnitaire(),
                ligne.getSousTotal()
        );
    }
}