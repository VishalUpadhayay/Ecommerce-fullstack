package com.ecommerce.controller;

import com.ecommerce.dto.WishlistItemDTO;
import com.ecommerce.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // GET /api/wishlist - View my wishlist
    @GetMapping
    public ResponseEntity<List<WishlistItemDTO>> getWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wishlistService.getWishlist(userDetails.getUsername()));
    }

    // POST /api/wishlist/add - Add product to wishlist
    @PostMapping("/add")
    public ResponseEntity<WishlistItemDTO> addToWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        Long productId = Long.valueOf(body.get("productId").toString());
        return ResponseEntity.ok(
                wishlistService.addToWishlist(userDetails.getUsername(), productId)
        );
    }

    // DELETE /api/wishlist/{productId} - Remove from wishlist
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        wishlistService.removeFromWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.ok("Removed from wishlist");
    }

    // GET /api/wishlist/check/{productId} - Check if product is in wishlist
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        boolean inWishlist = wishlistService.isInWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }
}