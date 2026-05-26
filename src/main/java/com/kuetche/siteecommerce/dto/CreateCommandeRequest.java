package com.kuetche.siteecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateCommandeRequest(
        @NotEmpty(message = "La commande doit contenir au moins un produit")
        List<@Valid CommandeItemRequest> items
) {
}