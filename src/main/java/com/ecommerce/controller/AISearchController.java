package com.ecommerce.controller;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai-search")
public class AISearchController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<?> aiSearch(@RequestBody Map<String, String> body) {
        String query = body.get("query");

        // Ask Gemini to extract structured filters from natural language
        String prompt = "Extract search filters from this shopping query as JSON only, no explanation, no markdown. " +
                "Format: {\"keyword\": \"string or empty\", \"maxPrice\": number or null, \"category\": \"Electronics/Clothing/Books/Home/Sports or null\"}. " +
                "Query: \"" + query + "\"";

        String aiResponse = geminiService.askGemini(prompt);

        List<Product> allProducts = productRepository.findAll();
        List<Product> filtered = allProducts;

        // Simple fallback parsing - extract price number and keywords from AI response or original query
        String searchText = (aiResponse != null ? aiResponse : query).toLowerCase();

        // Extract max price if mentioned (looks for numbers)
        Double maxPrice = extractNumber(query);
        if (maxPrice != null) {
            final double max = maxPrice;
            filtered = filtered.stream()
                    .filter(p -> p.getPrice().doubleValue() <= max)
                    .collect(Collectors.toList());
        }

        // Match keywords from original query against product name/description/category
        String[] queryWords = query.toLowerCase().split("\\s+");
        List<Product> keywordMatches = filtered.stream()
                .filter(p -> {
                    String combined = (p.getName() + " " + p.getDescription() + " " + p.getCategory()).toLowerCase();
                    for (String word : queryWords) {
                        if (word.length() > 2 && combined.contains(word)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        // If keyword matching found results, use those; otherwise fall back to price-filtered list
        List<Product> finalResults = !keywordMatches.isEmpty() ? keywordMatches : filtered;

        List<ProductDTO> dtos = finalResults.stream().map(this::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "products", dtos,
                "aiUnderstood", aiResponse != null ? aiResponse : "Searched based on your query"
        ));
    }

    private Double extractNumber(String text) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(text);
        if (m.find()) {
            return Double.parseDouble(m.group());
        }
        return null;
    }

    private ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setStock(p.getStock());
        dto.setCategory(p.getCategory());
        dto.setImageUrl(p.getImageUrl());
        return dto;
    }
}