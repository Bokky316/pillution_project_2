package com.javalab.student.dto;

import com.javalab.student.entity.Product;
import com.javalab.student.entity.ProductCategory;
import lombok.Getter;
import lombok.Setter;
import com.javalab.student.entity.ProductIngredient;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 클라이언트 응답용 상품 DTO
 */
@Getter
@Setter
public class ProductResponseDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Boolean active;
    private List<String> categories; // 카테고리 이름 리스트
    private List<String> ingredients; // 영양 성분 리스트 추가

    public static ProductResponseDTO fromEntity(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setActive(product.isActive());
        // 카테고리 매핑
        dto.setCategories(product.getCategories().stream()
                .map(ProductCategory::getName) // `getCategory()`가 아니라 `getName()`
                .collect(Collectors.toList()));
        // 영양 성분 추가
        dto.setIngredients(product.getIngredients().stream()
                .map(ProductIngredient::getIngredientName)
                .collect(Collectors.toList()));
        return dto;
    }
}
