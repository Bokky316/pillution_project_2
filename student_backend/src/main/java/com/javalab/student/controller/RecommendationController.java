package com.javalab.student.controller;

import com.javalab.student.dto.ProductRecommendationDTO;
import com.javalab.student.service.RecommendationService;
import com.javalab.student.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * 제품 추천 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;
    private final MemberService memberService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService, MemberService memberService) {
        this.recommendationService = recommendationService;
        this.memberService = memberService;
    }

    /**
     * 사용자에 대한 전체 제품 추천을 반환합니다.
     * @param authentication 현재 인증된 사용자의 정보
     * @return 추천 제품 목록 (필수 추천 및 추가 추천으로 분류)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRecommendations(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("User is not authenticated");
                response.put("status", "error");
                response.put("message", "User is not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String userEmail = authentication.getName();
            logger.info("Fetching recommendations for user: {}", userEmail);

            Map<String, List<ProductRecommendationDTO>> recommendations =
                    recommendationService.recommendProducts(userEmail);

            response.put("status", "success");
            response.put("data", recommendations);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while fetching recommendations", e);
            response.put("status", "error");
            response.put("message", "Error fetching recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 특정 카테고리 내에서 사용자에 대한 제품 추천을 반환합니다.
     * @param categoryId 카테고리 ID
     * @param authentication 현재 인증된 사용자의 정보
     * @return 해당 카테고리 내의 추천 제품 목록
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getRecommendationsByCategory(
            @PathVariable Long categoryId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("User is not authenticated");
                response.put("status", "error");
                response.put("message", "User is not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String userEmail = authentication.getName();
            logger.info("Fetching recommendations for user: {} in category: {}", userEmail, categoryId);

            // 여기서는 카테고리 ID를 사용하지 않고, 전체 추천을 가져옵니다.
            // 카테고리별 필터링은 프론트엔드에서 처리하거나,
            // RecommendationService에 새로운 메서드를 추가해야 할 수 있습니다.
            Map<String, List<ProductRecommendationDTO>> recommendations =
                    recommendationService.recommendProducts(userEmail);

            response.put("status", "success");
            response.put("data", recommendations);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error while fetching recommendations by category", e);
            response.put("status", "error");
            response.put("message", "Error fetching recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
