package com.kuetche.siteecommerce.integration;

import com.kuetche.siteecommerce.entity.Client;
import com.kuetche.siteecommerce.entity.Commande;
import com.kuetche.siteecommerce.entity.Produit;
import com.kuetche.siteecommerce.enums.Role;
import com.kuetche.siteecommerce.repository.ClientRepository;
import com.kuetche.siteecommerce.repository.CommandeRepository;
import com.kuetche.siteecommerce.repository.LigneCommandeRepository;
import com.kuetche.siteecommerce.repository.ProduitRepository;
import com.kuetche.siteecommerce.service.StripeService;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommandeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private LigneCommandeRepository ligneCommandeRepository;

    @MockitoBean
    private StripeService stripeService;

    private Client client;
    private Produit produit;

    @BeforeEach
    void setUp() {
        ligneCommandeRepository.deleteAll();
        commandeRepository.deleteAll();
        produitRepository.deleteAll();
        clientRepository.deleteAll();

        client = Client.builder()
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .tel("0600000000")
                .motDePasse("password")
                .role(Role.CLIENT)
                .active(true)
                .build();

        client = clientRepository.save(client);

        produit = Produit.builder()
                .name("T-shirt Nike")
                .price(BigDecimal.valueOf(29.99))
                .description("T-shirt sport")
                .stock(20)
                .color("Noir")
                .imageUrl("image.jpg")
                .imagePublicId("public-id")
                .build();

        produit = produitRepository.save(produit);

        Session sessionMock = mock(Session.class);
        when(sessionMock.getId()).thenReturn("cs_test_123");
        when(sessionMock.getUrl()).thenReturn("https://checkout.stripe.com/test");

        when(stripeService.creerSessionPaiement(any(Commande.class)))
                .thenReturn(sessionMock);
    }

    @Test
    void creerCommandeEtPayer_doitCreerCommandeEnBaseEtRetournerCreated() throws Exception {
        String jsonRequest = """
                {
                  "items": [
                    {
                      "produitId": %d,
                      "quantite": 2
                    }
                  ]
                }
                """.formatted(produit.getId());

        mockMvc.perform(post("/api/commandes/payer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("alex@test.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commandeId").exists())
                .andExpect(jsonPath("$.sessionId").value("cs_test_123"))
                .andExpect(jsonPath("$.url").value("https://checkout.stripe.com/test"));

        assertThat(commandeRepository.findAll()).hasSize(1);

        Commande commande = commandeRepository.findAll().get(0);

        assertThat(commande.getClient().getEmail()).isEqualTo("alex@test.com");
        assertThat(commande.getMontantTotal()).isEqualByComparingTo(BigDecimal.valueOf(59.98));
        assertThat(commande.getStripeSessionId()).isEqualTo("cs_test_123");
        assertThat(commande.getLignes()).hasSize(1);
        assertThat(commande.getLignes().get(0).getNomProduit()).isEqualTo("T-shirt Nike");
        assertThat(commande.getLignes().get(0).getQuantite()).isEqualTo(2);

        verify(stripeService, times(1)).creerSessionPaiement(any(Commande.class));
    }

    @Test
    void mesCommandes_doitRetournerLesCommandesDuClientConnecte() throws Exception {
        String jsonRequest = """
                {
                  "items": [
                    {
                      "produitId": %d,
                      "quantite": 1
                    }
                  ]
                }
                """.formatted(produit.getId());

        mockMvc.perform(post("/api/commandes/payer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("alex@test.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/commandes/mes-commandes")
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("alex@test.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void detailCommande_doitRetournerLaCommandeDuClientConnecte() throws Exception {
        String jsonRequest = """
                {
                  "items": [
                    {
                      "produitId": %d,
                      "quantite": 1
                    }
                  ]
                }
                """.formatted(produit.getId());

        mockMvc.perform(post("/api/commandes/payer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("alex@test.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isCreated());

        Commande commande = commandeRepository.findAll().get(0);

        mockMvc.perform(get("/api/commandes/{id}", commande.getId())
                        .with(jwt()
                                .jwt(jwt -> jwt.subject("alex@test.com"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commande.getId()));
    }
}