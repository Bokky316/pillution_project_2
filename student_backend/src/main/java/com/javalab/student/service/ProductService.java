package com.javalab.student.service;

import com.javalab.student.entity.Product;
import com.javalab.student.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // ProductService의 생성자
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 페이징 처리된 상품 목록 반환
     * @param page - 페이지 번호
     * @param size - 한 페이지당 항목 수
     * @return 상품 목록
     */
    public Page<Product> getProductList(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size)); // 페이징 처리된 상품 목록 반환
    }

    /**
     * 상품 상세 조회
     * @param productId - 상품 ID
     * @return 상품
     */
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId); // 상품 ID로 조회
    }

    /**
     * 상품 생성
     * @param product - 상품 정보
     * @return 생성된 상품
     */
    public Product createProduct(Product product) {
        return productRepository.save(product); // 새로운 상품 저장
    }

    /**
     * 상품 수정
     * @param productId - 수정할 상품 ID
     * @param updatedProduct - 수정된 상품 정보
     * @return 수정된 상품
     */
    public Optional<Product> updateProduct(Long productId, Product updatedProduct) {
        if (productRepository.existsById(productId)) {
            updatedProduct.setId(productId); // 기존 상품 ID로 업데이트
            return Optional.of(productRepository.save(updatedProduct)); // 수정된 상품 저장
        }
        return Optional.empty(); // 상품이 존재하지 않으면 비어있는 Optional 반환
    }

    /**
     * 상품 활성화/비활성화
     * @param productId - 활성화/비활성화할 상품 ID
     * @param status - 활성화 상태 (1: 활성화, 0: 비활성화)
     * @return 수정된 상품
     */
    public Optional<Product> toggleProductStatus(Long productId, int status) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setActive(status); // 상태 값 설정 (1: 활성화, 0: 비활성화)
            return Optional.of(productRepository.save(product)); // 수정된 상품 저장
        }
        return Optional.empty(); // 상품이 존재하지 않으면 비어있는 Optional 반환
    }
}
