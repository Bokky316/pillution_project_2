package com.javalab.student.service;

import com.javalab.student.dto.*;
import com.javalab.student.entity.Product;

import java.util.List;

public interface ProductService {

    ProductDto createProduct(ProductFormDto productFormDto);

    ProductDto updateProduct(Long id, ProductFormDto productFormDto);

    ProductDto getProductById(Long id);

    PageResponseDTO<ProductDto> getAllProducts(PageRequestDTO pageRequestDTO);

    void toggleProductActive(Long id);

    // 카테고리 포함된 상품 리스트 조회
    List<ProductResponseDTO> getProductList();

    List<ProductResponseDTO> getProductsByCategoryAndIngredient(Long categoryId, Long ingredientId);

    List<ProductResponseDTO> getProductsByCategory(Long categoryId);
}
