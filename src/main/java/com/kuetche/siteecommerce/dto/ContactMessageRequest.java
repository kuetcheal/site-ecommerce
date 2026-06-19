package com.kuetche.siteecommerce.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactMessageRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
        String email,

        @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
        String tel,

        @NotBlank(message = "Le type de demande est obligatoire")
        @Size(max = 100, message = "Le type de demande ne doit pas dépasser 100 caractères")
        String typeDemande,

        @NotBlank(message = "Le sujet est obligatoire")
        @Size(max = 180, message = "Le sujet ne doit pas dépasser 180 caractères")
        String sujet,

        @Size(max = 80, message = "Le numéro de commande ne doit pas dépasser 80 caractères")
        String numeroCommande,

        @NotBlank(message = "Le message est obligatoire")
        String message,

        @AssertTrue(message = "Vous devez accepter d'être recontacté")
        Boolean accepteContact
) {
}