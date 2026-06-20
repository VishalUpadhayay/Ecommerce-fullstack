package com.ecommerce.controller;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai-order-assistant")
public class AIOrderAssistantController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> askAboutOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {

        String question = body.get("question");

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);

        // Build a summary of the user's orders to give the AI context
        String orderContext = orders.isEmpty()
                ? "This user has no orders yet."
                : orders.stream().limit(5).map(o ->
                String.format("Order #%d: Status=%s, Total=₹%.2f, Date=%s, Items=%s",
                        o.getId(), o.getStatus(), o.getTotalAmount(), o.getCreatedAt().toLocalDate(),
                        o.getOrderItems() != null
                                ? o.getOrderItems().stream().map(i -> i.getProduct().getName()).collect(Collectors.joining(", "))
                                : "N/A")
        ).collect(Collectors.joining("\n"));

        String prompt = "You are an order assistant for ShopEase e-commerce. " +
                "Here is the customer's recent order history:\n" + orderContext +
                "\n\nAnswer the customer's question based ONLY on this data. Keep it under 3 sentences. " +
                "If the question is unrelated to their orders, politely say you can only help with order-related questions. " +
                "Customer question: " + question;

        String reply = geminiService.askGemini(prompt);

        if (reply == null) {
            reply = "Sorry, I couldn't process that right now. Please try again.";
        }

        return ResponseEntity.ok(Map.of("reply", reply));
    }
}