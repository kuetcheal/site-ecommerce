package com.kuetche.siteecommerce.dto;

import com.kuetche.siteecommerce.entity.ContactMessage;

import java.time.LocalDateTime;

public record ContactMessageResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String tel,
        String typeDemande,
        String sujet,
        String numeroCommande,
        String message,
        Boolean accepteContact,
        String statut,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ContactMessageResponse from(ContactMessage contactMessage) {
        return new ContactMessageResponse(
                contactMessage.getId(),
                contactMessage.getNom(),
                contactMessage.getPrenom(),
                contactMessage.getEmail(),
                contactMessage.getTel(),
                contactMessage.getTypeDemande(),
                contactMessage.getSujet(),
                contactMessage.getNumeroCommande(),
                contactMessage.getMessage(),
                contactMessage.getAccepteContact(),
                contactMessage.getStatut().name(),
                contactMessage.getCreatedAt(),
                contactMessage.getUpdatedAt()
        );
    }
}