package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductRatingDTO {
    private Double averageRating;
    private Long totalReviews;
}