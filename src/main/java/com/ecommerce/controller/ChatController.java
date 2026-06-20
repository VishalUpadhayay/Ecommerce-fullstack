package com.ecommerce.controller;

import com.ecommerce.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        String userMessage = body.get("message");

        String prompt = "You are a helpful customer support assistant for an e-commerce website called ShopEase. " +
                "Answer questions about shopping, orders, returns, and products briefly and helpfully. " +
                "Keep answers under 3 sentences. User question: " + userMessage;

        String reply = geminiService.askGemini(prompt);

        if (reply == null) {
            reply = "Sorry, I'm having trouble responding right now. Please try again in a moment.";
        }

        return ResponseEntity.ok(Map.of("reply", reply));
    }
}