package com.ecommerce.service;

import com.ecommerce.dto.WishlistItemDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all wishlist items for current user
    public List<WishlistItemDTO> getWishlist(String email) {
        User user = getUserByEmail(email);
        return wishlistItemRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Add product to wishlist
    @Transactional
    public WishlistItemDTO addToWishlist(String email, Long productId) {
        User user = getUserByEmail(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // Prevent duplicate wishlist entries
        if (wishlistItemRepository.existsByUserAndProductId(user, productId)) {
            throw new RuntimeException("Product already in wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        return toDTO(wishlistItemRepository.save(item));
    }

    // Remove product from wishlist
    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        User user = getUserByEmail(email);
        if (!wishlistItemRepository.existsByUserAndProductId(user, productId)) {
            throw new ResourceNotFoundException("Product not in wishlist");
        }
        wishlistItemRepository.deleteByUserAndProductId(user, productId);
    }

    // Check if a product is in user's wishlist
    public boolean isInWishlist(String email, Long productId) {
        User user = getUserByEmail(email);
        return wishlistItemRepository.existsByUserAndProductId(user, productId);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private WishlistItemDTO toDTO(WishlistItem item) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductPrice(item.getProduct().getPrice());
        dto.setProductImage(item.getProduct().getImageUrl());
        dto.setProductStock(item.getProduct().getStock());
        dto.setAddedAt(item.getAddedAt());
        return dto;
    }
}