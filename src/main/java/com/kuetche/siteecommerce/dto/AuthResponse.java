package com.kuetche.siteecommerce.dto;

public record AuthResponse(
        String token,
        String type,
        ClientResponse client
) {
}