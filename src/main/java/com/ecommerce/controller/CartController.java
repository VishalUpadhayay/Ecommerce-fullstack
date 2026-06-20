package com.ecommerce.controller;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // GET /api/cart - View my cart
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    // POST /api/cart/add - Add item to cart
    @PostMapping("/add")
    public ResponseEntity<CartItemDTO> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());

        return ResponseEntity.ok(
            cartService.addToCart(userDetails.getUsername(), productId, quantity)
        );
    }

    // PUT /api/cart/{itemId} - Update quantity
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body) {

        CartItemDTO updated = cartService.updateQuantity(
            userDetails.getUsername(), itemId, body.get("quantity")
        );

        if (updated == null) {
            return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
        }
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/cart/{itemId} - Remove item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        cartService.removeFromCart(userDetails.getUsername(), itemId);
        return ResponseEntity.ok("Item removed from cart");
    }

    // DELETE /api/cart - Clear entire cart
    @DeleteMapping
    public ResponseEntity<String> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok("Cart cleared");
    }
}
