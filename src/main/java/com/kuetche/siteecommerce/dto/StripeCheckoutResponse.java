package com.kuetche.siteecommerce.dto;

public record StripeCheckoutResponse(
        Long commandeId,
        String stripeSessionId,
        String checkoutUrl
) {
}