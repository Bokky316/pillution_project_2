package com.javalab.student.service.healthSurvey;

import com.javalab.student.dto.healthSurvey.HealthAnalysisDTO;
import com.javalab.student.dto.healthSurvey.ProductRecommendationDTO;
import com.javalab.student.dto.healthSurvey.RecommendationDTO;
import com.javalab.student.dto.healthSurvey.RecommendedIngredientDTO;
import com.javalab.student.entity.Member;
import com.javalab.student.entity.product.Product;
import com.javalab.student.entity.healthSurvey.*;
import com.javalab.student.repository.product.ProductRepository;
import com.javalab.student.repository.healthSurvey.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final NutrientScoreService nutrientScoreService;
    private final ProductRecommendationService productRecommendationService;
    private final AuthenticationService authenticationService;
    private final MemberInfoService memberInfoService;
    private final BmiCalculator bmiCalculator;
    private final HealthAnalysisService healthAnalysisService;

    private final RecommendationRepository recommendationRepository;
    private final RecommendedIngredientRepository recommendedIngredientRepository;
    private final RecommendedProductRepository recommendedProductRepository;
    private final MemberResponseRepository memberResponseRepository;
    private final MemberResponseOptionRepository memberResponseOptionRepository;
    private final ProductRepository productRepository;
    private final HealthRecordService healthRecordService;

    /**
     * 현재 로그인한 사용자의 건강 분석 및 추천 정보를 제공합니다.
     *
     * @return 건강 분석 및 추천 정보를 포함한 Map
     */
//    @Transactional
    public Map<String, Object> getHealthAnalysisAndRecommendations() {
        log.info("getHealthAnalysisAndRecommendations 메서드 시작");

        try {
            // 1. 현재 인증된 사용자 정보 가져오기
            Member member = authenticationService.getAuthenticatedMember();
            log.info("1. 인증된 사용자 ID: {}", member.getId());

            // 2. 사용자 응답 데이터 조회
            List<MemberResponse> textResponses = memberResponseRepository.findAgeHeightAndWeightResponses(member.getId());
            List<MemberResponseOption> optionResponses = memberResponseOptionRepository.findLatestResponsesByMemberId(member.getId());
            log.info("2. 사용자 응답 데이터 조회 완료. 응답 수: {}", textResponses.size());

            // 3. 사용자 정보 가져오기 (나이, 키, 몸무게 등)
            List<MemberResponse> responses = memberResponseRepository.findAgeHeightAndWeightResponses(member.getId());
            log.info("3. 사용자 정보 계산 시작");

            String name = memberInfoService.getName(member.getId());
            String gender = memberInfoService.getGender(member.getId());
            int age = memberInfoService.getAge(responses);
            double height = memberInfoService.getHeight(responses);
            double weight = memberInfoService.getWeight(responses);

            // BMI 계산
            double bmi = bmiCalculator.calculateBMI(height, weight);
            log.info("3. 사용자 정보 계산 완료. 사용자 정보: [이름: {}, 성별: {}, 나이: {}, 키: {}, 몸무게: {}, BMI: {:.2f}]",
                    name, gender, age, height, weight, bmi);

            // 4. 건강 분석 수행
            log.info("4. 건강 분석 시작");
            HealthAnalysisDTO healthAnalysis = healthAnalysisService.analyzeHealth(member.getId(), age, bmi, textResponses, optionResponses, gender);

            // 건강 분석 결과에 사용자 정보 추가
            healthAnalysis.setName(name);
            healthAnalysis.setAge(age);
            healthAnalysis.setGender(gender);
            healthAnalysis.setBmi(bmi);

            log.info("4. 건강 분석 완료: {}", healthAnalysis);

            // 5. 추천 영양 성분 점수 계산
            log.info("5. 추천 영양 성분 점수 계산 시작");
            Map<String, Integer> ingredientScores = nutrientScoreService.calculateIngredientScores(optionResponses, age, bmi, gender);

            log.info("5. 추천 영양 성분 점수 계산 완료. 점수 수: {}", ingredientScores.size());

            // 6. 추천 영양 성분 목록 가져오기
            log.info("6. 추천 영양 성분 목록 가져오기 시작");
            List<Map<String, Object>> recommendedIngredientsList = nutrientScoreService.getRecommendedIngredients(optionResponses, ingredientScores, age, bmi);

            log.info("6. 추천 영양 성분 목록 가져오기 완료. 추천 성분 수: {}", recommendedIngredientsList.size());

            // 7. 추천 엔티티 생성 및 저장
            log.info("7. 추천 엔티티 생성 시작");

            Recommendation recommendation = new Recommendation();

            recommendation.setMemberId(member.getId());
            recommendation.setCreatedAt(LocalDateTime.now());

            // recommendation 저장 후 ID를 가져옵니다.
            recommendation = recommendationRepository.saveAndFlush(recommendation);

            log.info("7. 추천 엔티티 저장 완료. ID: {}", recommendation.getId());

            // 8. 추천 영양 성분 저장
            log.info("8. 추천 영양 성분 저장 시작");

            List<RecommendedIngredient> recommendedIngredients = new ArrayList<>();

            for (Map<String, Object> ingredientMap : recommendedIngredientsList) {
                String ingredientName = (String) ingredientMap.get("name");
                Integer score = (Integer) ingredientMap.get("score");

                RecommendedIngredient ingredient = new RecommendedIngredient();
                ingredient.setRecommendation(recommendation);
                ingredient.setIngredientName(ingredientName);
                ingredient.setScore(score); // 점수 설정

                recommendedIngredients.add(ingredient);
            }

            recommendedIngredientRepository.saveAll(recommendedIngredients); // 일괄 저장
            log.info("8. 추천 영양 성분 저장 완료.");

            // 9. 추천 제품 생성 및 저장
            log.info("9. 추천 제품 생성 시작");

            List<ProductRecommendationDTO> productRecommendations = productRecommendationService.recommendProductsByIngredients(
                    new ArrayList<>(ingredientScores.keySet()), ingredientScores);

            List<RecommendedProduct> recommendedProducts = new ArrayList<>();

            for (ProductRecommendationDTO productDTO : productRecommendations) {
                Product product = productRepository.findById(productDTO.getId())
                        .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. ID: " + productDTO.getId()));

                RecommendedProduct recommendedProduct = RecommendedProduct.builder()
                        .reason(productDTO.getDescription())
                        .relatedIngredients(productDTO.getRecommendedIngredients())
                        .build();

                // 연관 관계 설정
                recommendedProduct.setRecommendation(recommendation);
                recommendedProduct.setProduct(product);

                recommendedProducts.add(recommendedProduct);
            }

            recommendedProductRepository.saveAll(recommendedProducts); // 일괄 저장
            log.info("9. 추천 제품 저장 완료.");

            // 10. 결과 반환 데이터 구성
            Map<String, Object> result = new HashMap<>();
            result.put("healthAnalysis", healthAnalysis);
            result.put("recommendedIngredients", recommendedIngredients);
            result.put("recommendations", recommendedProducts);

            log.info("10. 결과 데이터 구성 완료");

            return result;

        } catch (Exception e) {
            log.error("getHealthAnalysisAndRecommendations 메서드 실행 중 오류 발생", e);
            throw new RuntimeException("건강 분석 및 추천 생성 중 오류가 발생했습니다.", e);
        }
    }


    /**
     * 현재 로그인한 사용자의 건강 기록 히스토리를 조회합니다.
     *
     * @return 사용자의 모든 건강 기록 리스트를 반환
     */
    @Transactional(readOnly = true)
    public List<RecommendationDTO> getHealthHistory() {
        Member member = authenticationService.getAuthenticatedMember();

        // 사용자의 모든 Recommendation 기록 조회
        List<Recommendation> recommendations = recommendationRepository.findByMemberId(member.getId());

        // Recommendation -> RecommendationDTO로 변환
        return recommendations.stream().map(recommendation -> {
            RecommendationDTO dto = new RecommendationDTO();
            dto.setId(recommendation.getId());
            dto.setMemberId(recommendation.getMemberId());
            dto.setCreatedAt(recommendation.getCreatedAt());

            // 추천 제품 정보 추가
            List<ProductRecommendationDTO> productRecommendations =
                    recommendedProductRepository.findByRecommendationId(recommendation.getId())
                            .stream()
                            .map(product -> new ProductRecommendationDTO(
                                    product.getProduct().getId(),
                                    null,
                                    product.getReason(),
                                    null,
                                    0,
                                    null,
                                    null))
                            .toList();
            dto.setProductRecommendations(productRecommendations);

            // 추천 영양 성분 정보 추가
            List<RecommendedIngredientDTO> recommendedIngredients =
                    recommendedIngredientRepository.findByRecommendationId(recommendation.getId())
                            .stream()
                            .map(ingredient -> {
                                RecommendedIngredientDTO ingredientDTO = new RecommendedIngredientDTO();
                                ingredientDTO.setIngredientName(ingredient.getIngredientName());
                                ingredientDTO.setScore(ingredient.getScore());
                                return ingredientDTO;
                            })
                            .toList();
            dto.setRecommendedIngredients(recommendedIngredients);

            return dto;
        }).toList();
    }
}