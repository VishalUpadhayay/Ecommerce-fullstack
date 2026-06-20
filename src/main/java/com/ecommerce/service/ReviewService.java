package com.ecommerce.service;

import com.ecommerce.dto.ProductRatingDTO;
import com.ecommerce.dto.ReviewDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all reviews for a product
    public List<ReviewDTO> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Get average rating + total review count for a product
    public ProductRatingDTO getProductRating(Long productId) {
        Double avg = reviewRepository.getAverageRating(productId);
        Long count = reviewRepository.countByProductId(productId);
        return new ProductRatingDTO(avg != null ? Math.round(avg * 10) / 10.0 : 0.0, count);
    }

    // Add a review (one review per user per product)
    @Transactional
    public ReviewDTO addReview(String email, Long productId, Integer rating, String comment) {
        User user = getUserByEmail(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (reviewRepository.existsByUserAndProductId(user, productId)) {
            throw new RuntimeException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment)
                .build();

        return toDTO(reviewRepository.save(review));
    }

    // Update own review
    @Transactional
    public ReviewDTO updateReview(String email, Long productId, Integer rating, String comment) {
        User user = getUserByEmail(email);
        Review review = reviewRepository.findByUserAndProductId(user, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setRating(rating);
        review.setComment(comment);
        return toDTO(reviewRepository.save(review));
    }

    // Delete own review
    @Transactional
    public void deleteReview(String email, Long productId) {
        User user = getUserByEmail(email);
        Review review = reviewRepository.findByUserAndProductId(user, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        reviewRepository.delete(review);
    }

    // Check if current user already reviewed this product
    public boolean hasUserReviewed(String email, Long productId) {
        User user = getUserByEmail(email);
        return reviewRepository.existsByUserAndProductId(user, productId);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserName(review.getUser().getName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}