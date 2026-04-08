package com.backend.handicrafts.service;

import com.backend.handicrafts.entity.Product;
import com.backend.handicrafts.entity.User;
import com.backend.handicrafts.exception.ResourceNotFoundException;
import com.backend.handicrafts.repository.ProductRepository;
import com.backend.handicrafts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    public long countByCategory(String category) {
        return productRepository.countByCategoryIgnoreCase(category);
    }

    public BigDecimal averagePriceByCategory(String category) {
        BigDecimal value = productRepository.averagePriceByCategory(category);
        return value == null ? BigDecimal.ZERO : value;
    }

    public Page<Product> getProductsPaged(int page, int size, String sortBy, String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
    }

    public Product createProduct(Product product) {
        product.setId(null);
        Product savedProduct = productRepository.save(product);
        log.info("DATA INSERTED SUCCESSFULLY: Product{{id={}, name={}}}",
            savedProduct.getId(), savedProduct.getName());

        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                emailService.sendNewProductEmail(user.getEmail(), savedProduct.getName());
            } catch (Exception ex) {
                log.error("Email failed for: {}", user.getEmail(), ex);
            }
        }

        return savedProduct;
    }

    public Product updateProduct(Long id, Product payload) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        existing.setName(payload.getName());
        existing.setDescription(payload.getDescription());
        existing.setPrice(payload.getPrice());
        existing.setImageUrl(payload.getImageUrl());
        existing.setCategory(payload.getCategory());

        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(existing);
    }
}
