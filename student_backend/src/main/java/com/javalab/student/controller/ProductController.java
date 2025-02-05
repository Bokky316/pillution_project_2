package com.javalab.student.controller;

import com.javalab.student.entity.Product;
import com.javalab.student.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 목록을 반환하는 API (페이징 처리)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 페이징 처리된 상품 목록 조회
            Page<Product> productPage = productService.getProductList(page, size);
            List<Product> products = productPage.getContent();  // 현재 페이지에 해당하는 상품 목록

            // 응답 데이터 구성
            response.put("status", "success");
            response.put("data", products);
            response.put("total", productPage.getTotalElements()); // 총 상품 수
            response.put("totalPages", productPage.getTotalPages()); // 총 페이지 수
            response.put("currentPage", productPage.getNumber()); // 현재 페이지 번호

            return ResponseEntity.ok(response);  // 200 OK와 함께 페이징 처리된 상품 목록 반환
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching all products", e);
            response.put("status", "error");
            response.put("message", "Unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 상품 상세 조회 API
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductDetails(@PathVariable Long productId) {
        try {
            Optional<Product> product = productService.getProductById(productId);
            return product.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching product with id: " + productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 상품 활성화/비활성화 처리 API
    @PutMapping("/{productId}/status/{status}")
    public ResponseEntity<Map<String, Object>> updateProductStatus(
            @PathVariable Long productId,
            @PathVariable int status) {

        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Product> updatedProduct = productService.toggleProductStatus(productId, status);
            if (updatedProduct.isPresent()) {
                response.put("status", "success");
                response.put("data", updatedProduct.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Product not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error updating product status", e);
            response.put("status", "error");
            response.put("message", "Error updating product status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}