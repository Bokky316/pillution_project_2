package com.javalab.student.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 상품 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private boolean active;
    private String mainImageUrl;
    private List<ProductCategoryDto> categories;
}

