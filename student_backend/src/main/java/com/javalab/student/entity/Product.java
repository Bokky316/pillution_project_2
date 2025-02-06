package com.javalab.student.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 영양제 상품 정보를 나타내는 엔티티 클래스입니다.
 * 이 클래스는 상품의 기본 정보, 가격, 재고, 상태 등을 포함합니다.
 * 또한 상품과 관련된 영양 성분 및 카테고리와의 관계를 정의합니다.
 */
@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /** 상품의 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 상품명 */
    @Column(nullable = false, length = 255)
    private String name;

    /** 상품 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 상품 가격 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 재고 수량 */
    @Column(nullable = false)
    private int stock;

    /** 상품 활성화 상태 (1: 활성, 0: 비활성) */
    @Column(nullable = false)
    private boolean active;

    /** 대표 이미지 경로 */
//    private String imgPath;
//
//    /** 대표 이미지명 */
//    private String fileName;
//
//    /** 상품에 포함된 여러 이미지 */
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ProductImage> imgList;

    /**
     * 상품에 포함된 영양 성분 목록
     * 다대다 관계로, product_ingredient_mapping 테이블을 통해 매핑됩니다.
     */
    @ManyToMany
    @JoinTable(
            name = "product_ingredient_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "id")
    )
    private List<ProductIngredient> ingredients;

    /**
     * 상품이 속한 카테고리 목록
     * 다대다 관계로, product_category_mapping 테이블을 통해 매핑됩니다.
     */
    @ManyToMany
    @JoinTable(
            name = "product_category_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<ProductCategory> categories;

    /**
     * 추천 시스템을 위한 점수
     * 이 필드는 데이터베이스에 저장되지 않고 런타임에만 사용됩니다.
     */
    @Transient
    private int score;
}

