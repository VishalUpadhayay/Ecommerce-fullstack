package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get all products (old method, no pagination) - kept for backward compatibility
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // NEW: Get paginated products
    public Map<String, Object> getProductsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductDTO> products = productPage.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Map.of(
                "products", products,
                "currentPage", productPage.getNumber(),
                "totalPages", productPage.getTotalPages(),
                "totalItems", productPage.getTotalElements(),
                "pageSize", productPage.getSize()
        );
    }

    // Get product by ID
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDTO(product);
    }

    // Search products by keyword (old method)
    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // NEW: Search products with pagination
    public Map<String, Object> searchProductsPaginated(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);

        List<ProductDTO> products = productPage.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Map.of(
                "products", products,
                "currentPage", productPage.getNumber(),
                "totalPages", productPage.getTotalPages(),
                "totalItems", productPage.getTotalElements(),
                "pageSize", productPage.getSize()
        );
    }

    // Get products by category (old method)
    public List<ProductDTO> getByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // NEW: Get products by category with pagination
    public Map<String, Object> getByCategoryPaginated(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByCategory(category, pageable);

        List<ProductDTO> products = productPage.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Map.of(
                "products", products,
                "currentPage", productPage.getNumber(),
                "totalPages", productPage.getTotalPages(),
                "totalItems", productPage.getTotalElements(),
                "pageSize", productPage.getSize()
        );
    }

    // Admin: Create product
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = toEntity(dto);
        return toDTO(productRepository.save(product));
    }

    // Admin: Update product
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());

        return toDTO(productRepository.save(product));
    }

    // Admin: Delete product
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // Convert Entity → DTO
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

    // Convert DTO → Entity
    private Product toEntity(ProductDTO dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .build();
    }
}