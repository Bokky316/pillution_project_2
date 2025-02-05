package com.javalab.student.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private int active;

    // 메인 이미지 경로 (대표 이미지)
    private String mainImageUrl;

    // 주성분과의 관계 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_ingredient_id")
    private Product mainIngredient;

    // 카테고리와의 관계 (N:M)
    @ManyToMany
    @JoinTable(
            name = "product_category_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<ProductCategory> categories;

//    // 상품 이미지 리스트 (1:N)
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ProductImg> images;
//}


    // 추천 시스템을 위한 점수 필드
    @Transient// 이 필드는 데이터베이스에 저장되지 않습니다.
    private int score;

}