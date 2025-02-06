package com.javalab.student.controller;

import com.javalab.student.dto.*;
import com.javalab.student.entity.Product;
import com.javalab.student.repository.ProductRepository;
import com.javalab.student.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.util.List;

/**
 * 상품 관리 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductController(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    /**
     * 특정 상품 상세 정보 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductDetails(@PathVariable Long productId) {
        try {
            return productRepository.findById(productId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (DataAccessException e) {
            log.error("Database error occurred while fetching product with id: " + productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching product with id: " + productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상품 등록
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid ProductFormDto productFormDto) {
        log.info("상품 등록 요청 수신: {}", productFormDto);

        try {
            ProductDto savedProduct = productService.createProduct(productFormDto);
            log.info("상품 등록 성공: {}", savedProduct);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            log.error("상품 등록 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 상품 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable("id") Long id, @RequestBody @Valid ProductFormDto productFormDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productFormDto));
    }

    /**
     * 페이징 처리된 상품 목록 조회
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductDto>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();
        log.info("페이지 요청: {}", pageRequestDTO);

        PageResponseDTO<ProductDto> responseDTO = productService.getAllProducts(pageRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 상품 활성/비활성
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleProductActive(@PathVariable("id") Long id) {
        productService.toggleProductActive(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponseDTO>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long ingredientId) {

        List<ProductResponseDTO> filteredProducts = productService.getProductsByCategoryAndIngredient(categoryId, ingredientId);
        return ResponseEntity.ok(filteredProducts);
    }

}

