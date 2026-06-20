package com.ecommerce.controller;

import com.ecommerce.dto.ProductRatingDTO;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // PUBLIC: Get all reviews for a product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    // PUBLIC: Get average rating + count for a product
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ProductRatingDTO> getProductRating(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductRating(productId));
    }

    // USER: Add a review (must be logged in)
    @PostMapping
    public ResponseEntity<ReviewDTO> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody Map<String, Object> body) {

        Long productId = Long.valueOf(body.get("productId").toString());
        Integer rating = Integer.parseInt(body.get("rating").toString());
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";

        ReviewDTO review = reviewService.addReview(userDetails.getUsername(), productId, rating, comment);
        return ResponseEntity.ok(review);
    }

    // USER: Update own review
    @PutMapping("/product/{productId}")
    public ResponseEntity<ReviewDTO> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {

        Integer rating = Integer.parseInt(body.get("rating").toString());
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";

        ReviewDTO review = reviewService.updateReview(userDetails.getUsername(), productId, rating, comment);
        return ResponseEntity.ok(review);
    }

    // USER: Delete own review
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<String> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        reviewService.deleteReview(userDetails.getUsername(), productId);
        return ResponseEntity.ok("Review deleted");
    }

    // USER: Check if current user reviewed this product
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> hasReviewed(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        boolean reviewed = reviewService.hasUserReviewed(userDetails.getUsername(), productId);
        return ResponseEntity.ok(Map.of("hasReviewed", reviewed));
    }
}