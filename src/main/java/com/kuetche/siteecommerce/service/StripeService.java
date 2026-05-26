package com.kuetche.siteecommerce.service;

import com.kuetche.siteecommerce.entity.Commande;
import com.kuetche.siteecommerce.entity.LigneCommande;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    public Session creerSessionPaiement(Commande commande) {
        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("commandeId", commande.getId().toString());

            for (LigneCommande ligne : commande.getLignes()) {
                long unitAmount = ligne.getPrixUnitaire()
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();

                SessionCreateParams.LineItem.PriceData.ProductData productData =
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(ligne.getNomProduit())
                                .build();

                SessionCreateParams.LineItem.PriceData priceData =
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(unitAmount)
                                .setProductData(productData)
                                .build();

                SessionCreateParams.LineItem lineItem =
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(ligne.getQuantite().longValue())
                                .setPriceData(priceData)
                                .build();

                paramsBuilder.addLineItem(lineItem);
            }

            return Session.create(paramsBuilder.build());

        } catch (StripeException e) {
            throw new RuntimeException("Erreur lors de la création de la session Stripe", e);
        }
    }
}