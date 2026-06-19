package com.kuetche.siteecommerce.entity;

import com.kuetche.siteecommerce.enums.StatutContact;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String tel;

    @Column(name = "type_demande", nullable = false, length = 100)
    private String typeDemande;

    @Column(nullable = false, length = 180)
    private String sujet;

    @Column(name = "numero_commande", length = 80)
    private String numeroCommande;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "accepte_contact", nullable = false)
    private Boolean accepteContact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatutContact statut = StatutContact.NOUVEAU;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}