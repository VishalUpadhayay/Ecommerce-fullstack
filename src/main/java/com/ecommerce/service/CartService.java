package com.ecommerce.service;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all cart items for the current user
    public List<CartItemDTO> getCart(String email) {
        User user = getUserByEmail(email);
        return cartItemRepository.findByUser(user)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // Add item to cart (or increase quantity if already there)
    @Transactional
    public CartItemDTO addToCart(String email, Long productId, int quantity) {
        User user = getUserByEmail(email);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        Optional<CartItem> existing = cartItemRepository.findByUserAndProductId(user, productId);

        CartItem cartItem;
        if (existing.isPresent()) {
            cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .build();
        }

        return toDTO(cartItemRepository.save(cartItem));
    }

    // Update quantity of a cart item
    @Transactional
    public CartItemDTO updateQuantity(String email, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        item.setQuantity(quantity);
        return toDTO(cartItemRepository.save(item));
    }

    // Remove item from cart
    @Transactional
    public void removeFromCart(String email, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        cartItemRepository.delete(item);
    }

    // Clear entire cart
    @Transactional
    public void clearCart(String email) {
        User user = getUserByEmail(email);
        cartItemRepository.deleteAllByUser(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private CartItemDTO toDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductPrice(item.getProduct().getPrice());
        dto.setProductImage(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getProduct().getPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())));
        return dto;
    }
}
