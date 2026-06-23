package com.ecommerce.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    // Initialize Stripe with our secret key when app starts
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // Create a Stripe Checkout Session - returns a URL to redirect user to
    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        try {
            // amount comes in rupees from frontend, Stripe needs smallest unit (paise)
            Double amountInRupees = Double.parseDouble(body.get("amount").toString());
            long amountInPaise = (long) (amountInRupees * 100);

            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("https://ecommerce-frontend-to5d.onrender.com/payment-success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("https://ecommerce-frontend-to5d.onrender.com/cart")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(amountInPaise)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("ShopEase Order")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            return ResponseEntity.ok(Map.of("checkoutUrl", session.getUrl()));

        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}