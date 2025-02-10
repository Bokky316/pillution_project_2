package com.javalab.student.service;

import com.javalab.student.dto.*;
import com.javalab.student.entity.Product;
import com.javalab.student.entity.ProductCategory;
import com.javalab.student.entity.ProductIngredient;
import com.javalab.student.repository.ProductCategoryRepository;
import com.javalab.student.repository.ProductIngredientRepository;
import com.javalab.student.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 상품 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductIngredientRepository ingredientRepository;
    private final ModelMapper modelMapper;
    private final String uploadDir = "/path/to/upload/directory"; // 실제 이미지 저장 경로

    @Autowired
    private ProductCategoryRepository productCategoryRepository;


    public ProductServiceImpl(ProductRepository productRepository, ProductCategoryRepository categoryRepository, ProductIngredientRepository ingredientRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * 새로운 상품을 생성하고 저장합니다.
     */
    @Override
    public ProductDto createProduct(ProductFormDto productFormDto, MultipartFile mainImage) {
        // 1. 이미지 업로드 처리
        String imageUrl = uploadImage(mainImage);
        productFormDto.setMainImageUrl(imageUrl); // productFormDto에 이미지 URL 설정

        // 2. DTO → Entity 변환
        Product product = modelMapper.map(productFormDto, Product.class);

        // 3. 카테고리 및 영양 성분 매핑
        List<ProductCategory> categories = categoryRepository.findAllById(productFormDto.getCategoryIds());
        List<ProductIngredient> ingredients = ingredientRepository.findAllById(productFormDto.getIngredientIds());
        product.setCategories(categories);
        product.setIngredients(ingredients);

        // 4. 저장 후 DTO 반환
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Value("${uploadPath}")
    private String uploadPathConfig;

    private String uploadImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지가 비어 있습니다.");
        }
        try {
            // 원본 파일 이름 정리
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

            // properties에 설정된 uploadPath 사용
            String processedPath = uploadPathConfig.replace("file://", "");
            // Windows의 경우 "/c:/shop/"와 같이 앞에 붙은 불필요한 슬래시 제거
            if (processedPath.startsWith("/") && processedPath.charAt(2) == ':') {
                processedPath = processedPath.substring(1);
            }

            Path filePath = Paths.get(processedPath, fileName);
            Files.createDirectories(filePath.getParent());
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 리소스 매핑에 맞추어 URL 반환
            return "http://localhost:8080/images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 중 오류 발생", e);
        }
    }



    /**
     * 기존 상품 정보를 업데이트합니다.
     */
    @Override
    public ProductDto updateProduct(Long id, ProductFormDto formDto) {
        // 기존 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 기본 필드 업데이트
        product.setName(formDto.getName());
        product.setDescription(formDto.getDescription());
        product.setPrice(formDto.getPrice());
        product.setStock(formDto.getStock());
        product.setActive(formDto.isActive());
        product.setMainImageUrl(formDto.getMainImageUrl());

        // 카테고리 업데이트 처리
        if (formDto.getCategoryIds() != null && !formDto.getCategoryIds().isEmpty()) {
            List<ProductCategory> updatedCategories = productCategoryRepository.findAllById(formDto.getCategoryIds());
            product.setCategories(updatedCategories);
        } else {
            product.setCategories(Collections.emptyList());
        }

        // 기타(예: 영양 성분 등)의 업데이트도 필요하면 동일하게 처리

        productRepository.save(product);
        return convertToProductDto(product); // product를 ProductDto로 변환하는 메서드
    }

    private ProductDto convertToProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .active(product.isActive())
                .mainImageUrl(product.getMainImageUrl())
                .categories(
                        product.getCategories().stream()
                                .map(cat -> new ProductCategoryDto(cat.getId(), cat.getName()))
                                .collect(Collectors.toList())
                )
                .build();
    }


    /**
     * 특정 상품 정보를 조회합니다.
     */
    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return modelMapper.map(product, ProductDto.class);
    }

    /**
     * 페이징 처리를 적용하여 모든 상품 목록을 조회합니다.
     */
    public PageResponseDTO<ProductDto> getAllProducts(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("id");
        Page<Product> result = productRepository.findAllWithCategories(pageable);

        List<ProductDto> dtoList = result.getContent().stream().map(product -> {
            ProductDto productDto = modelMapper.map(product, ProductDto.class);
            List<ProductCategoryDto> categoryDtos = product.getCategories().stream()
                    .map(category -> modelMapper.map(category, ProductCategoryDto.class))
                    .collect(Collectors.toList());
            productDto.setCategories(categoryDtos);
            System.out.println("Product: " + product.getName() + ", Categories: " + categoryDtos);
            return productDto;
        }).collect(Collectors.toList());

        return PageResponseDTO.<ProductDto>builder()
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .pageRequestDTO(pageRequestDTO)
                .build();
    }




    /**
     * 특정 상품을 활성/비활성화 합니다.
     */
    @Override
    public void toggleProductActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(!product.isActive()); // 활성화/비활성화 토글
        productRepository.save(product);
    }

    public List<ProductResponseDTO> getProductList() {
        List<Product> products = productRepository.findAllWithCategoriesAndIngredients();
        return products.stream().map(ProductResponseDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategoryAndIngredient(Long categoryId, Long ingredientId) {
        List<Product> products;

        if (categoryId != null && ingredientId != null) {
            products = productRepository.findByCategoryAndIngredient(categoryId, ingredientId);
        } else if (categoryId != null) {
            products = productRepository.findByCategory(categoryId);
        } else if (ingredientId != null) {
            products = productRepository.findByIngredient(ingredientId);
        } else {
            products = productRepository.findAll();
        }

        return products.stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 카테고리에 포함된 영양 성분을 기준으로 상품 필터링
     */
    @Override
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategories_Id(categoryId);
        return products.stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public PageResponseDTO<ProductDto> searchProducts(String field, String query, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("id");
        Page<Product> result;
        if ("상품명".equals(field)) {
            result = productRepository.findByNameContainingIgnoreCase(query, pageable);
        } else if ("카테고리".equals(field)) {
            result = productRepository.findByCategoryNameContainingIgnoreCase(query, pageable);
        } else if ("영양성분".equals(field)) {
            result = productRepository.findByIngredientNameContainingIgnoreCase(query, pageable);
        } else {
            result = productRepository.findAll(pageable);
        }

        List<ProductDto> dtoList = result.getContent().stream().map(product -> {
            ProductDto productDto = modelMapper.map(product, ProductDto.class);
            // 카테고리 등의 필요 필드 매핑(이미 기존 코드와 동일)
            if (product.getCategories() != null) {
                productDto.setCategories(product.getCategories().stream()
                        .map(cat -> modelMapper.map(cat, ProductCategoryDto.class))
                        .collect(Collectors.toList()));
            }
            return productDto;
        }).collect(Collectors.toList());

        return PageResponseDTO.<ProductDto>builder()
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .pageRequestDTO(pageRequestDTO)
                .build();
    }


}
