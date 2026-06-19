package com.kuetche.siteecommerce.dto;

import com.kuetche.siteecommerce.enums.StatutContact;
import jakarta.validation.constraints.NotNull;

public record UpdateContactStatusRequest(
        @NotNull(message = "Le statut est obligatoire")
        StatutContact statut
) {
}