package com.kuetche.siteecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @Email(message = "L'email est invalide")
        @NotBlank(message = "L'email est obligatoire")
        String email,

        @NotBlank(message = "Le téléphone est obligatoire")
        String tel,

        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse
) {
}