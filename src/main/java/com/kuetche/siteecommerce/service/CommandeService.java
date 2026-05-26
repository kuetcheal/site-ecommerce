package com.kuetche.siteecommerce.service;

import com.kuetche.siteecommerce.dto.CommandeResponse;
import com.kuetche.siteecommerce.dto.CreateCommandeRequest;
import com.kuetche.siteecommerce.dto.StripeCheckoutResponse;
import com.kuetche.siteecommerce.entity.Client;
import com.kuetche.siteecommerce.entity.Commande;
import com.kuetche.siteecommerce.entity.LigneCommande;
import com.kuetche.siteecommerce.entity.Produit;
import com.kuetche.siteecommerce.enums.StatutCommande;
import com.kuetche.siteecommerce.enums.StatutPaiement;
import com.kuetche.siteecommerce.repository.ClientRepository;
import com.kuetche.siteecommerce.repository.CommandeRepository;
import com.kuetche.siteecommerce.repository.ProduitRepository;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final ClientRepository clientRepository;
    private final StripeService stripeService;

    public StripeCheckoutResponse creerCommandeEtSessionPaiement(Jwt jwt, CreateCommandeRequest request) {
        String email = jwt.getSubject();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));

        Commande commande = Commande.builder()
                .client(client)
                .montantTotal(BigDecimal.ZERO)
                .statut(StatutCommande.EN_ATTENTE)
                .statutPaiement(StatutPaiement.EN_ATTENTE)
                .lignes(new ArrayList<>())
                .build();

        BigDecimal montantTotal = BigDecimal.ZERO;

        for (var item : request.items()) {
            Produit produit = produitRepository.findById(item.produitId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));

            if (produit.getStock() < item.quantite()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock insuffisant pour le produit : " + produit.getName()
                );
            }

            BigDecimal sousTotal = produit.getPrice()
                    .multiply(BigDecimal.valueOf(item.quantite()));

            LigneCommande ligne = LigneCommande.builder()
                    .commande(commande)
                    .produit(produit)
                    .nomProduit(produit.getName())
                    .quantite(item.quantite())
                    .prixUnitaire(produit.getPrice())
                    .sousTotal(sousTotal)
                    .build();

            commande.getLignes().add(ligne);
            montantTotal = montantTotal.add(sousTotal);
        }

        commande.setMontantTotal(montantTotal);

        Commande savedCommande = commandeRepository.save(commande);

        Session session = stripeService.creerSessionPaiement(savedCommande);

        savedCommande.setStripeSessionId(session.getId());
        commandeRepository.save(savedCommande);

        return new StripeCheckoutResponse(
                savedCommande.getId(),
                session.getId(),
                session.getUrl()
        );
    }

    public List<CommandeResponse> mesCommandes(Jwt jwt) {
        String email = jwt.getSubject();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));

        return commandeRepository.findByClientOrderByCreatedAtDesc(client)
                .stream()
                .map(CommandeResponse::from)
                .toList();
    }

    public CommandeResponse detailCommande(Jwt jwt, Long id) {
        String email = jwt.getSubject();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));

        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));

        if (!commande.getClient().getId().equals(client.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette commande");
        }

        return CommandeResponse.from(commande);
    }
}