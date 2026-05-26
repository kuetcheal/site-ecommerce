package com.kuetche.siteecommerce.dto;

import com.kuetche.siteecommerce.entity.Produit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProduitResponse(
        Long id,
        String name,
        BigDecimal price,
        String description,
        Integer stock,
        String color,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProduitResponse from(Produit produit) {
        return new ProduitResponse(
                produit.getId(),
                produit.getName(),
                produit.getPrice(),
                produit.getDescription(),
                produit.getStock(),
                produit.getColor(),
                produit.getImageUrl(),
                produit.getCreatedAt(),
                produit.getUpdatedAt()
        );
    }
}