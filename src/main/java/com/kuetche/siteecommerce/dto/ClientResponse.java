package com.kuetche.siteecommerce.dto;

import com.kuetche.siteecommerce.entity.Client;

public record ClientResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String tel,
        String role
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getNom(),
                client.getPrenom(),
                client.getEmail(),
                client.getTel(),
                client.getRole().name()
        );
    }
}