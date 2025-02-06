package com.javalab.student.service;

import com.javalab.student.dto.ProductRecommendationDTO;
import com.javalab.student.entity.*;
import com.javalab.student.repository.ProductIngredientRepository;
import com.javalab.student.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 제품 추천 관련 비즈니스 로직을 처리하는 서비스 클래스
 */

@Slf4j
@Service
public class RecommendationService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberResponseRepository memberResponseRepository;
    @Autowired
    private MemberResponseOptionRepository memberResponseOptionRepository;
    @Autowired
    private QuestionOptionIngredientRepository questionOptionIngredientRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductIngredientRepository productIngredientRepository;

    /**
     * 사용자 이메일을 기반으로 제품을 추천합니다.
     *
     * @param userEmail 사용자 이메일
     * @return 추천 제품 목록 (필수 추천 및 추가 추천으로 분류)
     * @throws EntityNotFoundException 사용자를 찾을 수 없는 경우
     */
    public Map<String, List<ProductRecommendationDTO>> recommendProducts(String userEmail) {
        Member member = Optional.ofNullable(memberRepository.findByEmail(userEmail))
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));

        Long memberId = member.getId();
        double bmi = calculateBMI(memberId);
        int age = getAge(memberId);
        Map<String, Integer> ingredientScores = calculateIngredientScores(memberId);
        adjustIngredientScores(ingredientScores, age, bmi);
        List<Product> scoredProducts = calculateProductScores(productRepository.findAll(), ingredientScores);
        return classifyRecommendations(scoredProducts);
    }

    /**
     * 사용자 응답을 기반으로 BMI를 계산합니다.
     */
    private double calculateBMI(Long memberId) {
        List<MemberResponse> responses = memberResponseRepository.findAgeHeightAndWeightResponses(memberId);

        log.info("Age, height and weight responses for member {}: {}", memberId, responses);

        double height = responses.stream()
                .filter(r -> r.getQuestion().getId() == 4L)
                .findFirst()
                .map(r -> Double.parseDouble(r.getResponseText()))
                .orElseThrow(() -> new IllegalStateException("키 정보를 찾을 수 없습니다."));

        double weight = responses.stream()
                .filter(r -> r.getQuestion().getId() == 5L)
                .findFirst()
                .map(r -> Double.parseDouble(r.getResponseText()))
                .orElseThrow(() -> new IllegalStateException("몸무게 정보를 찾을 수 없습니다."));

        log.info("Calculated height: {}, weight: {} for member {}", height, weight, memberId);

        double heightInMeter = height / 100.0;
        return weight / (heightInMeter * heightInMeter);
    }
    /**
     * 사용자 응답을 기반으로 나이를 가져옵니다.
     */
    private int getAge(Long memberId) {
        List<MemberResponse> responses = memberResponseRepository.findAgeHeightAndWeightResponses(memberId);

        return responses.stream()
                .filter(r -> r.getQuestion().getId() == 3L)
                .findFirst()
                .map(r -> {
                    try {
                        return Integer.parseInt(r.getResponseText());
                    } catch (NumberFormatException e) {
                        log.error("Failed to parse age: {}", r.getResponseText());
                        throw new IllegalStateException("나이 정보가 올바르지 않습니다.");
                    }
                })
                .orElseThrow(() -> new IllegalStateException("나이 정보를 찾을 수 없습니다."));
    }

    /**
     * 사용자 응답을 기반으로 각 성분의 점수를 계산합니다.
     */
    private Map<String, Integer> calculateIngredientScores(Long memberId) {
        List<MemberResponse> latestTextResponses = memberResponseRepository.findLatestResponsesByMemberId(memberId);
        List<MemberResponseOption> latestOptionResponses = memberResponseOptionRepository.findLatestResponsesByMemberId(memberId);
        Map<String, Integer> ingredientScores = new HashMap<>();

        // 텍스트 응답 처리
        for (MemberResponse response : latestTextResponses) {
            if (!response.getQuestion().getQuestionType().equals("PERSONAL")) {
                List<String> ingredients = questionOptionIngredientRepository.findIngredientsByQuestionIdAndResponseText(
                        response.getQuestion().getId(), response.getResponseText());
                for (String ingredient : ingredients) {
                    ingredientScores.put(ingredient, ingredientScores.getOrDefault(ingredient, 0) + 1);
                }
            }
        }

        // 선택형 응답 처리
        for (MemberResponseOption response : latestOptionResponses) {
            List<String> ingredients = questionOptionIngredientRepository.findIngredientsByQuestionIdAndResponseText(
                    response.getQuestion().getId(), response.getOption().getOptionText());
            for (String ingredient : ingredients) {
                ingredientScores.put(ingredient, ingredientScores.getOrDefault(ingredient, 0) + 1);
            }
        }

        return ingredientScores;
    }

    /**
     * 연령대와 BMI를 고려하여 성분 점수를 조정합니다.
     */
    private void adjustIngredientScores(Map<String, Integer> ingredientScores, int age, double bmi) {
        String ageGroup = determineAgeGroup(age);
        String bmiCategory = determineBMICategory(bmi);

        for (String ingredient : ingredientScores.keySet()) {
            int ageImportance = getAgeImportance(ingredient, ageGroup);
            int bmiAdjustment = getBMIAdjustment(ingredient, bmiCategory);

            int currentScore = ingredientScores.get(ingredient);
            ingredientScores.put(ingredient, currentScore + ageImportance + bmiAdjustment);
        }
    }

    /**
     * 주어진 제품 목록에 대해 성분 점수를 기반으로 제품 점수를 계산합니다.
     */
    private List<Product> calculateProductScores(List<Product> products, Map<String, Integer> ingredientScores) {
        for (Product product : products) {
            int score = 0;
            List<ProductIngredient> productIngredients = product.getIngredients();
            for (ProductIngredient productIngredient : productIngredients) {
                score += ingredientScores.getOrDefault(productIngredient.getIngredientName(), 0);
            }
            product.setScore(score);
        }

        return products.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * 점수가 계산된 제품 목록을 필수 추천과 추가 추천으로 분류합니다.
     */
    private Map<String, List<ProductRecommendationDTO>> classifyRecommendations(List<Product> scoredProducts) {
        Map<String, List<ProductRecommendationDTO>> recommendations = new HashMap<>();
        List<ProductRecommendationDTO> essentialRecommendations = new ArrayList<>();
        List<ProductRecommendationDTO> additionalRecommendations = new ArrayList<>();

        int totalRecommendations = Math.min(scoredProducts.size(), 8);

        for (int i = 0; i < totalRecommendations; i++) {
            Product product = scoredProducts.get(i);
            ProductRecommendationDTO dto = convertToDTO(product);

            if (i < 3) {
                essentialRecommendations.add(dto);
            } else {
                additionalRecommendations.add(dto);
            }
        }

        recommendations.put("essential", essentialRecommendations);
        recommendations.put("additional", additionalRecommendations);
        return recommendations;
    }


    /**
     * Product 엔티티를 ProductRecommendationDTO로 변환합니다.
     */
    private ProductRecommendationDTO convertToDTO(Product product) {
        String mainIngredient = product.getIngredients().isEmpty() ? "" : product.getIngredients().get(0).getIngredientName();
        return new ProductRecommendationDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getScore(),
                mainIngredient
        );
    }

    /**
     * 나이를 기반으로 연령대를 결정합니다.
     */
    private String determineAgeGroup(int age) {
        if (age < 20) return "10대";
        else if (age < 30) return "20대";
        else if (age < 40) return "30대";
        else if (age < 50) return "40대";
        else return "50대 이상";
    }

    /**
     * BMI를 기반으로 비만도 카테고리를 결정합니다.
     */
    private String determineBMICategory(double bmi) {
        if (bmi < 18.5) return "저체중";
        else if (bmi < 23) return "정상";
        else if (bmi < 25) return "과체중";
        else return "비만";
    }

    /**
     * 연령대별 영양소 중요도를 반환합니다.
     */
    private int getAgeImportance(String ingredient, String ageGroup) {
        // 이 메서드의 구현은 각 연령대별로 특정 영양소의 중요도를 정의해야 합니다.
        // 예를 들어, 칼슘은 10대와 50대 이상에서 더 중요할 수 있습니다.
        return 0; // 임시 반환값
    }

    /**
     * BMI 카테고리에 따른 영양소 점수 조정값을 반환합니다.
     */
    private int getBMIAdjustment(String ingredient, String bmiCategory) {
        // 이 메서드의 구현은 각 BMI 카테고리별로 특정 영양소의 조정값을 정의해야 합니다.
        // 예를 들어, 저체중인 경우 단백질 점수를 높일 수 있습니다.
        return 0; // 임시 반환값
    }

    /**
     * 성분 점수를 기반으로 제품을 추천합니다.
     */
    /**
     * 성분 점수를 기반으로 제품을 추천합니다.
     */
    public List<ProductRecommendationDTO> recommendProductsByIngredients(Map<String, Integer> ingredientScores) {
        List<ProductRecommendationDTO> recommendations = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : ingredientScores.entrySet()) {
            String ingredient = entry.getKey();
            int score = entry.getValue();

            List<Product> products = productRepository.findByIngredientName(ingredient);
            products.sort((p1, p2) -> Integer.compare(calculateProductScore(p2, ingredientScores), calculateProductScore(p1, ingredientScores)));

            for (int i = 0; i < Math.min(2, products.size()); i++) {
                Product product = products.get(i);
                ProductRecommendationDTO dto = convertToDTO(product);
                dto.setScore(calculateProductScore(product, ingredientScores));
                recommendations.add(dto);
            }
        }

        recommendations.sort((r1, r2) -> Integer.compare(r2.getScore(), r1.getScore()));
        return recommendations;
    }

    /**
     * 제품의 점수를 계산합니다.
     */
    private int calculateProductScore(Product product, Map<String, Integer> ingredientScores) {
        return 3 + product.getIngredients().stream()
                .mapToInt(ingredient -> ingredientScores.getOrDefault(ingredient.getIngredientName(), 0))
                .sum();
    }
}
