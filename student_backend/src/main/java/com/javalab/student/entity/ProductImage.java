package com.javalab.student.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne  // 하나의 상품에 여러 이미지가 있을 수 있음
    @JoinColumn(name = "product_id") // 외래 키 컬럼 이름
    private Product product;

    @Column(nullable = false)
    private String imageUrl; // 이미지 URL

    @Column(name = "image_type")
    private String imageType; // 이미지 종류 (main, sub 등)

    @Column(name = "image_order")
    private int order; // 이미지 순서
}
