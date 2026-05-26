package com.kuetche.siteecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CommandeItemRequest(
        @NotNull(message = "L'id du produit est obligatoire")
        Long produitId,

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être au minimum de 1")
        Integer quantite
) {
}