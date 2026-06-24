package com.kuetche.siteecommerce.integration;

import com.kuetche.siteecommerce.entity.ContactMessage;
import com.kuetche.siteecommerce.enums.StatutContact;
import com.kuetche.siteecommerce.repository.ContactMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContactMessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @BeforeEach
    void setUp() {
        contactMessageRepository.deleteAll();
    }

    @Test
    void creerMessage_doitCreerMessageEnBaseEtRetournerCreated() throws Exception {
        String jsonRequest = """
                {
                  "nom": "Kuetche",
                  "prenom": "Alex",
                  "email": "alex@test.com",
                  "tel": "0600000000",
                  "typeDemande": "Information produit",
                  "sujet": "Demande d'information",
                  "numeroCommande": "CMD-001",
                  "message": "Bonjour, je souhaite avoir plus d'informations sur vos produits.",
                  "accepteContact": true
                }
                """;

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Kuetche"))
                .andExpect(jsonPath("$.prenom").value("Alex"))
                .andExpect(jsonPath("$.email").value("alex@test.com"))
                .andExpect(jsonPath("$.typeDemande").value("Information produit"))
                .andExpect(jsonPath("$.sujet").value("Demande d'information"))
                .andExpect(jsonPath("$.numeroCommande").value("CMD-001"))
                .andExpect(jsonPath("$.accepteContact").value(true))
                .andExpect(jsonPath("$.statut").value("NOUVEAU"));

        assertThat(contactMessageRepository.findAll()).hasSize(1);

        ContactMessage message = contactMessageRepository.findAll().get(0);

        assertThat(message.getNom()).isEqualTo("Kuetche");
        assertThat(message.getPrenom()).isEqualTo("Alex");
        assertThat(message.getEmail()).isEqualTo("alex@test.com");
        assertThat(message.getTel()).isEqualTo("0600000000");
        assertThat(message.getTypeDemande()).isEqualTo("Information produit");
        assertThat(message.getSujet()).isEqualTo("Demande d'information");
        assertThat(message.getNumeroCommande()).isEqualTo("CMD-001");
        assertThat(message.getMessage()).isEqualTo("Bonjour, je souhaite avoir plus d'informations sur vos produits.");
        assertThat(message.getAccepteContact()).isTrue();
        assertThat(message.getStatut()).isEqualTo(StatutContact.NOUVEAU);
    }

    @Test
    void creerMessage_doitRetournerBadRequestSiEmailInvalide() throws Exception {
        String jsonRequest = """
                {
                  "nom": "Kuetche",
                  "prenom": "Alex",
                  "email": "email-invalide",
                  "tel": "0600000000",
                  "typeDemande": "Information produit",
                  "sujet": "Demande d'information",
                  "numeroCommande": "CMD-001",
                  "message": "Bonjour, je souhaite avoir plus d'informations.",
                  "accepteContact": true
                }
                """;

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        assertThat(contactMessageRepository.findAll()).isEmpty();
    }

    @Test
    void creerMessage_doitRetournerBadRequestSiAccepteContactFalse() throws Exception {
        String jsonRequest = """
                {
                  "nom": "Kuetche",
                  "prenom": "Alex",
                  "email": "alex@test.com",
                  "tel": "0600000000",
                  "typeDemande": "Information produit",
                  "sujet": "Demande d'information",
                  "numeroCommande": "CMD-001",
                  "message": "Bonjour, je souhaite avoir plus d'informations.",
                  "accepteContact": false
                }
                """;

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        assertThat(contactMessageRepository.findAll()).isEmpty();
    }

    @Test
    void listerMessagesAdmin_doitRetournerTousLesMessages() throws Exception {
        ContactMessage message1 = ContactMessage.builder()
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .typeDemande("Information produit")
                .sujet("Sujet 1")
                .numeroCommande("CMD-001")
                .message("Message 1")
                .accepteContact(true)
                .statut(StatutContact.NOUVEAU)
                .build();

        ContactMessage message2 = ContactMessage.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@test.com")
                .tel("0700000000")
                .typeDemande("Commande")
                .sujet("Sujet 2")
                .numeroCommande("CMD-002")
                .message("Message 2")
                .accepteContact(true)
                .statut(StatutContact.TRAITE)
                .build();

        contactMessageRepository.save(message1);
        contactMessageRepository.save(message2);

        mockMvc.perform(get("/api/contacts/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listerMessagesAdminParStatut_doitRetournerMessagesFiltres() throws Exception {
        ContactMessage messageNouveau = ContactMessage.builder()
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .typeDemande("Information produit")
                .sujet("Sujet nouveau")
                .numeroCommande("CMD-001")
                .message("Message nouveau")
                .accepteContact(true)
                .statut(StatutContact.NOUVEAU)
                .build();

        ContactMessage messageTraite = ContactMessage.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@test.com")
                .tel("0700000000")
                .typeDemande("Commande")
                .sujet("Sujet traité")
                .numeroCommande("CMD-002")
                .message("Message traité")
                .accepteContact(true)
                .statut(StatutContact.TRAITE)
                .build();

        contactMessageRepository.save(messageNouveau);
        contactMessageRepository.save(messageTraite);

        mockMvc.perform(get("/api/contacts/admin")
                        .param("statut", "NOUVEAU")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].statut").value("NOUVEAU"));
    }

    @Test
    void obtenirMessageAdmin_doitRetournerMessageParId() throws Exception {
        ContactMessage message = ContactMessage.builder()
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .typeDemande("Information produit")
                .sujet("Demande test")
                .numeroCommande("CMD-001")
                .message("Message test")
                .accepteContact(true)
                .statut(StatutContact.NOUVEAU)
                .build();

        ContactMessage savedMessage = contactMessageRepository.save(message);

        mockMvc.perform(get("/api/contacts/admin/{id}", savedMessage.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMessage.getId()))
                .andExpect(jsonPath("$.nom").value("Kuetche"))
                .andExpect(jsonPath("$.email").value("alex@test.com"));
    }

    @Test
    void modifierStatutAdmin_doitModifierStatutMessage() throws Exception {
        ContactMessage message = ContactMessage.builder()
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .typeDemande("Information produit")
                .sujet("Demande test")
                .numeroCommande("CMD-001")
                .message("Message test")
                .accepteContact(true)
                .statut(StatutContact.NOUVEAU)
                .build();

        ContactMessage savedMessage = contactMessageRepository.save(message);

        String jsonRequest = """
                {
                  "statut": "TRAITE"
                }
                """;

        mockMvc.perform(patch("/api/contacts/admin/{id}/statut", savedMessage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TRAITE"));

        ContactMessage updatedMessage = contactMessageRepository
                .findById(savedMessage.getId())
                .orElseThrow();

        assertThat(updatedMessage.getStatut()).isEqualTo(StatutContact.TRAITE);
    }

    @Test
    void supprimerMessageAdmin_doitSupprimerMessageEnBase() throws Exception {
        ContactMessage message = ContactMessage.builder()
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .typeDemande("Information produit")
                .sujet("Demande test")
                .numeroCommande("CMD-001")
                .message("Message test")
                .accepteContact(true)
                .statut(StatutContact.NOUVEAU)
                .build();

        ContactMessage savedMessage = contactMessageRepository.save(message);

        mockMvc.perform(delete("/api/contacts/admin/{id}", savedMessage.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        assertThat(contactMessageRepository.findById(savedMessage.getId())).isEmpty();
    }

    @Test
    void routeAdmin_doitRetournerUnauthorizedSansToken() throws Exception {
        mockMvc.perform(get("/api/contacts/admin"))
                .andExpect(status().isUnauthorized());
    }
}