package com.javalab.student.service.healthSurvey;

import com.javalab.student.dto.healthSurvey.HealthAnalysisDTO;
import com.javalab.student.entity.healthSurvey.MemberResponse;
import com.javalab.student.entity.healthSurvey.Recommendation;
import com.javalab.student.entity.healthSurvey.RecommendedIngredient;
import com.javalab.student.repository.healthSurvey.RecommendedIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 영양 성분 점수 계산을 위한 서비스 클래스
 */
@Service
@Transactional
public class NutrientScoreService {

    @Autowired
    private RecommendedIngredientRepository recommendedIngredientRepository;

    /**
     * 회원의 응답, 나이, BMI를 기반으로 영양 성분 점수를 계산합니다.
     *
     * @param responses 회원의 설문 응답 목록
     * @param age 회원의 나이
     * @param bmi 회원의 BMI
     * @return 각 영양 성분의 점수를 포함하는 Map
     */
    public Map<String, Integer> calculateIngredientScores(List<MemberResponse> responses, int age, double bmi) {
        Map<String, Integer> ingredientScores = new HashMap<>();
        Set<String> mainSymptoms = getMainSymptoms(responses);

        // 주요 증상에 대한 점수 계산
        calculateMainSymptomScores(responses, ingredientScores, mainSymptoms);

        // 추가 증상에 대한 점수 계산
        calculateAdditionalSymptomScores(responses, ingredientScores);

        // 생활 습관에 대한 점수 계산
        calculateLifestyleScores(responses, ingredientScores);

        // 나이와 BMI에 따른 점수 조정
        adjustScoresByAgeAndBMI(ingredientScores, age, bmi);

        // 주요 증상에 해당하는 영양 성분을 추출하고 점수를 높게 설정
        Set<String> mainSymptomIngredients = getMainSymptomIngredients(responses);
        for (String ingredient : mainSymptomIngredients) {
            ingredientScores.compute(ingredient, (k, v) -> Math.max((v == null) ? 3 : v, 3)); // 최소 3점 보장
        }

        return ingredientScores;
    }


    /**
     * 주요 증상에 해당하는 영양 성분을 추출합니다.
     *
     * @param responses 회원의 응답 목록
     * @return 주요 증상에 해당하는 영양 성분 집합
     */
    private Set<String> getMainSymptomIngredients(List<MemberResponse> responses) {
        Set<String> mainSymptomIngredients = new HashSet<>();

        for (MemberResponse response : responses) {
            String subCategory = response.getQuestion().getSubCategory().getName();
            if (subCategory.equals("혈관·혈액순환")) {
                mainSymptomIngredients.add("오메가-3");
                mainSymptomIngredients.add("비타민B군");
            } else if (subCategory.equals("소화·장")) {
                mainSymptomIngredients.add("글루타민");
                mainSymptomIngredients.add("프로바이오틱스");
            } else if (subCategory.equals("피부")) {
                mainSymptomIngredients.add("콜라겐");
                mainSymptomIngredients.add("비오틴");
            } else if (subCategory.equals("눈")) {
                mainSymptomIngredients.add("루테인");
                mainSymptomIngredients.add("오메가-3");
            } else if (subCategory.equals("두뇌 활동")) {
                mainSymptomIngredients.add("인지질(PS)");
                mainSymptomIngredients.add("오메가-3");
            } else if (subCategory.equals("피로감")) {
                mainSymptomIngredients.add("비타민B군");
                mainSymptomIngredients.add("마그네슘");
            } else if (subCategory.equals("뼈·관절")) {
                mainSymptomIngredients.add("칼슘");
                mainSymptomIngredients.add("비타민D");
            } else if (subCategory.equals("면역")) {
                mainSymptomIngredients.add("아연");
                mainSymptomIngredients.add("비타민C");
            } else if (subCategory.equals("모발")) {
                mainSymptomIngredients.add("비오틴");
                mainSymptomIngredients.add("아연");
            }
        }

        return mainSymptomIngredients;
    }


    /**
     * 계산된 영양 성분 점수를 기반으로 추천 영양 성분을 결정합니다.
     *
     * @param healthAnalysis 건강 분석 결과
     * @param ingredientScores 계산된 영양 성분 점수
     * @param age 회원의 나이
     * @param bmi 회원의 BMI
     * @return 추천 영양 성분 목록 (최대 5개, 이름과 점수 포함)
     */
    public List<Map<String, Object>> getRecommendedIngredients(HealthAnalysisDTO healthAnalysis, Map<String, Integer> ingredientScores, int age, double bmi) {
        Set<String> mainSymptomIngredients = getMainSymptomIngredients(healthAnalysis.getResponses());

        if (ingredientScores.isEmpty() && mainSymptomIngredients.isEmpty()) {
            ingredientScores.put("칼슘", 1);
            ingredientScores.put("마그네슘", 1);
            ingredientScores.put("비타민D", 1);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        // 모든 영양 성분을 점수 순으로 정렬
        List<Map.Entry<String, Integer>> sortedIngredients = ingredientScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        // 주요 증상 영양 성분을 우선 추가
        for (Map.Entry<String, Integer> entry : sortedIngredients) {
            if (mainSymptomIngredients.contains(entry.getKey()) && result.size() < 5) {
                Map<String, Object> ingredient = new HashMap<>();
                ingredient.put("name", entry.getKey());
                ingredient.put("score", entry.getValue());
                result.add(ingredient);
            }
        }

        // 남은 자리에 다른 영양 성분 추가
        for (Map.Entry<String, Integer> entry : sortedIngredients) {
            if (!mainSymptomIngredients.contains(entry.getKey()) && result.size() < 5) {
                Map<String, Object> ingredient = new HashMap<>();
                ingredient.put("name", entry.getKey());
                ingredient.put("score", entry.getValue());
                result.add(ingredient);
            }
        }

        // 결과가 비어있으면 기본값 추가
        if (result.isEmpty()) {
            Map<String, Object> defaultIngredient = new HashMap<>();
            defaultIngredient.put("name", "기본 영양소");
            defaultIngredient.put("score", 1);
            result.add(defaultIngredient);
        }

        return result;
    }

    /**
     * 추천 영양 성분을 저장합니다.
     *
     * @param ingredientScores 계산된 영양 성분 점수
     * @param healthAnalysis 건강 분석 결과
     * @param age 회원의 나이
     * @param bmi 회원의 BMI
     */
    @Transactional
    public void saveRecommendedIngredients(Recommendation recommendation, Map<String, Integer> ingredientScores, HealthAnalysisDTO healthAnalysis, int age, double bmi) {
        List<Map<String, Object>> recommendedIngredients = getRecommendedIngredients(healthAnalysis, ingredientScores, age, bmi);

        // recommendedIngredients의 점수만 고려하여 maxScore 계산
        double maxScore = recommendedIngredients.stream()
                .mapToInt(map -> (Integer) map.get("score"))
                .max()
                .orElse(1);

        for (Map<String, Object> ingredientMap : recommendedIngredients) {
            String ingredientName = (String) ingredientMap.get("name");
            Integer originalScore = (Integer) ingredientMap.get("score");

            RecommendedIngredient ingredient = new RecommendedIngredient();
            ingredient.setRecommendation(recommendation);
            ingredient.setIngredientName(ingredientName);

            double normalizedScore = Math.min(5.0, Math.max(0.0, (originalScore / (double) maxScore) * 5));
            double roundedScore = Math.round(normalizedScore * 10.0) / 10.0;
            ingredient.setScore(roundedScore);

            recommendedIngredientRepository.save(ingredient);
            recommendation.getRecommendedIngredients().add(ingredient);
        }
    }


    /**
     * 주요 증상을 추출합니다.
     *
     * @param responses 회원의 설문 응답 목록
     * @return 주요 증상 집합
     */
    private Set<String> getMainSymptoms(List<MemberResponse> responses) {
        return responses.stream()
                .filter(r -> r.getQuestion().getSubCategory().getName().equals("주요 증상"))
                .flatMap(r -> Arrays.stream(r.getResponseText().split(",")))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    /**
     * 주요 증상에 대한 점수를 계산합니다.
     *
     * @param responses        회원의 설문 응답 목록
     * @param ingredientScores 영양 성분 점수 Map
     * @param mainSymptoms     주요 증상 집합
     */
    private void calculateMainSymptomScores(List<MemberResponse> responses, Map<String, Integer> ingredientScores,
                                            Set<String> mainSymptoms) {
        for (MemberResponse response : responses) {
            String subCategory = response.getQuestion().getSubCategory().getName();
            if (mainSymptoms.contains(subCategory)) {
                switch (subCategory) {
                    case "혈관·혈액순환":
                        calculateBloodCirculationScores(response, ingredientScores);
                        break;
                    case "소화·장":
                        calculateDigestionScores(response, ingredientScores);
                        break;
                    case "피부":
                        calculateSkinScores(response, ingredientScores);
                        break;
                    case "눈":
                        calculateEyeScores(response, ingredientScores);
                        break;
                    case "두뇌 활동":
                        calculateBrainScores(response, ingredientScores);
                        break;
                    case "피로감":
                        calculateFatigueScores(response, ingredientScores);
                        break;
                    case "뼈·관절":
                        calculateBoneJointScores(response, ingredientScores);
                        break;
                    case "면역":
                        calculateImmuneScores(response, ingredientScores);
                        break;
                    case "모발":
                        calculateHairScores(response, ingredientScores);
                        break;
                }
            }
        }
    }


    /**
     * 혈관·혈액순환 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateBloodCirculationScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "손발 끝이 자주 저려요":
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "상처가 잘 낫지 않아요":
                    ingredientScores.compute("코엔자임Q10", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 5 : v + 5);
                    break;
                case "잇몸이 붓고 피가 나요":
                    ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "얼굴이 자주 창백해져요":
                    ingredientScores.compute("철분", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민B12", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "선택할 것은 없지만 혈관·혈액순환이 걱정돼요":
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 소화·장 건강 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateDigestionScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "복통이나 속 쓰림이 자주 발생해요":
                    ingredientScores.compute("글루타민", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("프로바이오틱스", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "변비가 있어요":
                    ingredientScores.compute("프로바이오틱스", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("식이섬유", (k, v) -> (v == null) ? 5 : v + 5);
                    break;
                case "변이 묽은 편이에요":
                    ingredientScores.compute("프로바이오틱스", (k, v) -> (v == null) ? 5 : v + 5);
                    break;
                case "술을 마시면 얼굴이나 몸이 붉어지고 소화가 안 돼요":
                    ingredientScores.compute("비타민B6", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "잔뇨감이 있어요":
                    ingredientScores.compute("크랜베리 추출물", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "선택할 것은 없지만 소화력 개선이 필요해요":
                    ingredientScores.compute("프로바이오틱스", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 피부 건강 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateSkinScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "피부가 건조하고 머리에 비듬이 많이 생겨요":
                    ingredientScores.compute("콜라겐", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비오틴", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "여드름이 많아서 걱정이에요":
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("비타민A", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "피부에 염증이 자주 생겨요":
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "입안이 헐고 입술이 자주 갈라져요":
                    ingredientScores.compute("비타민B2", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "선택할 것은 없지만 피부건강이 걱정돼요":
                    ingredientScores.compute("콜라겐", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 눈 건강 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateEyeScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "눈이 건조해 뻑뻑하고 가려워요":
                    ingredientScores.compute("루테인", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "눈 주변이 떨려요":
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "핸드폰, 모니터를 본 후 시야가 흐릿해요":
                    ingredientScores.compute("루테인", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민A", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "어두워지면 시력이 저하돼요":
                    ingredientScores.compute("비타민A", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("루테인", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "선택할 것은 없지만 눈 건강이 걱정돼요":
                    ingredientScores.compute("루테인", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 두뇌 건강 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateBrainScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "기억력이 떨어지는 것 같아요":
                    ingredientScores.compute("인지질(PS)", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "두통이 자주 생겨요":
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민B2", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "불안이나 긴장을 자주 느껴요":
                    ingredientScores.compute("GABA", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "우울한 감정을 자주 느껴요":
                    ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "귀에서 울리는 소리가 가끔 나요":
                    ingredientScores.compute("비타민B12", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "선택할 것은 없지만 두뇌 활동이 걱정돼요":
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 피로감 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateFatigueScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "무기력하고 식욕이 없어요":
                    ingredientScores.compute("철분", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "자고 일어나도 피곤해요":
                    ingredientScores.compute("코엔자임Q10", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "신경이 예민하고 잠을 잘 이루지 못해요":
                    ingredientScores.compute("GABA", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "소변을 보기 위해 잠을 깨요":
                    ingredientScores.compute("크랜베리 추출물", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "선택할 것은 없지만 피로감이 있어요":
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 뼈·관절 건강 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateBoneJointScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "뼈가 부러진 경험이 있어요":
                    ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 5 : v + 5);
                    break;
                case "뼈가 약하다고 느껴요":
                    ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "최근 1년 중 스테로이드를 섭취한 기간이 3개월 이상이에요":
                    ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "선택할 것은 없지만 뼈·관절이 걱정돼요":
                    ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 면역 건강 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateImmuneScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "스트레스가 매우 많아요":
                    ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "감염성 질환에 자주 걸려요":
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "선택할 것은 없지만 면역이 걱정돼요":
                    ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 모발 건강 관련 영양 성분 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateHairScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] symptoms = response.getResponseText().split(",");
        for (String symptom : symptoms) {
            switch (symptom.trim()) {
                case "머리카락에 힘이 없고 잘 빠져요":
                    ingredientScores.compute("비오틴", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "머리카락이 윤기 없고 갈라지고 끊어져요":
                    ingredientScores.compute("판토텐산(비타민B5)", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비오틴", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "새치가 많이 나요":
                    ingredientScores.compute("구리", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("비오틴", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "선택할 것은 없지만 모발 건강이 걱정돼요":
                    ingredientScores.compute("비오틴", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 추가 증상에 대한 점수를 계산합니다.
     *
     * @param responses 회원의 설문 응답 목록
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateAdditionalSymptomScores(List<MemberResponse> responses, Map<String, Integer> ingredientScores) {
        for (MemberResponse response : responses) {
            if (response.getQuestion().getSubCategory().getName().equals("추가 증상")) {
                String[] symptoms = response.getResponseText().split(",");
                for (String symptom : symptoms) {
                    switch (symptom.trim()) {
                        case "혈압이 높아요":
                            ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 5 : v + 5);
                            ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 4 : v + 4);
                            break;
                        case "혈압이 낮아요":
                            ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 4 : v + 4);
                            ingredientScores.compute("철분", (k, v) -> (v == null) ? 3 : v + 3);
                            break;
                        case "더위를 타고 땀을 많이 흘려요":
                            ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 4 : v + 4);
                            ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 3 : v + 3);
                            break;
                        case "항응고제를 복용 중이에요":
                            ingredientScores.compute("비타민K", (k, v) -> (v == null) ? 5 : v + 5);
                            break;
                        case "알레르기가 있어요":
                            ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 4 : v + 4);
                            ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 3 : v + 3);
                            break;
                    }
                }
            }
        }
    }

    /**
     * 생활 습관에 대한 점수를 계산합니다.
     *
     * @param responses 회원의 설문 응답 목록
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateLifestyleScores(List<MemberResponse> responses, Map<String, Integer> ingredientScores) {
        for (MemberResponse response : responses) {
            String subCategory = response.getQuestion().getSubCategory().getName();
            switch (subCategory) {
                case "운동 빈도":
                    calculateExerciseFrequencyScores(response, ingredientScores);
                    break;
                case "야외활동 시간":
                    calculateOutdoorActivityScores(response, ingredientScores);
                    break;
                case "식습관":
                    calculateDietScores(response, ingredientScores);
                    break;
                case "기호식품":
                    calculatePreferencesScores(response, ingredientScores);
                    break;
                case "생활 패턴":
                    calculatePatternsScores(response, ingredientScores);
                    break;
                case "가족력":
                    calculateFamilyHistoryScores(response, ingredientScores);
                    break;
                case "여성건강":
                    calculateFemaleHealthScores(response, ingredientScores);
                    break;
                case "남성건강":
                    calculateMaleHealthScores(response, ingredientScores);
                    break;
            }
        }
    }

    /**
     * 운동 빈도에 대한 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateExerciseFrequencyScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        switch (response.getResponseText()) {
            case "주 1회 이하":
                ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 5 : v + 5);
                ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 4 : v + 4);
                break;
            case "주 2~3회":
                ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 2 : v + 2);
                ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 1 : v + 1);
                break;
        }
    }

    /**
     * 야외활동 시간에 대한 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateOutdoorActivityScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        switch (response.getResponseText()) {
            case "1시간 이하":
                ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 4 : v + 4);
                break;
            case "1~4시간":
                ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 2 : v + 2);
                break;
        }
    }


    /**
     * 식습관에 대한 점수를 계산합니다.
     * 체크하지 않은 항목 기준으로 점수 부여
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateDietScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        Set<String> selectedHabits = new HashSet<>(Arrays.asList(response.getResponseText().split(",")));

        if (!selectedHabits.contains("생선을 자주 먹어요")) {
            ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 5 : v + 5);
        }
        if (!selectedHabits.contains("채소를 자주 먹어요")) {
            ingredientScores.compute("식이섬유", (k, v) -> (v == null) ? 4 : v + 4);
            ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 4 : v + 4);
        }
        if (!selectedHabits.contains("과일을 자주 먹어요")) {
            ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 5 : v + 5);
            ingredientScores.compute("식이섬유", (k, v) -> (v == null) ? 3 : v + 3);
        }
        if (!selectedHabits.contains("고기를 자주 먹어요")) {
            ingredientScores.compute("철분", (k, v) -> (v == null) ? 5 : v + 5);
            ingredientScores.compute("비타민B12", (k, v) -> (v == null) ? 4 : v + 4);
        }
        if (selectedHabits.contains("단 음식을 자주 먹어요")) {
            ingredientScores.compute("크롬", (k, v) -> (v == null) ? 4 : v + 4);
            ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 3 : v + 3);
        }
        if (selectedHabits.contains("식사를 자주 걸러요")) {
            ingredientScores.compute("종합비타민", (k, v) -> (v == null) ? 5 : v + 5);
            ingredientScores.compute("철분", (k, v) -> (v == null) ? 4 : v + 4);
        }
    }
    /**
     * 기호식품에 대한 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculatePreferencesScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] preferences = response.getResponseText().split(",");
        for (String preference : preferences) {
            switch (preference.trim()) {
                case "담배를 피워요":
                    ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("코엔자임Q10", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "커피를 마셔요":
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "물을 잘 안 마셔요":
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("식이섬유", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "인스턴트 음식을 자주 먹어요":
                    ingredientScores.compute("프로바이오틱스", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 생활 패턴에 대한 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculatePatternsScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] patterns = response.getResponseText().split(",");
        for (String pattern : patterns) {
            switch (pattern.trim()) {
                case "업무, 학업 강도가 높아요":
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "핸드폰, 모니터를 오래 봐요":
                    ingredientScores.compute("루테인", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("비타민A", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "목이 자주 건조하거나 칼칼해요":
                    ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "집중력이 필요한 시기예요":
                    ingredientScores.compute("인지질(PS)", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "식사량을 줄이는 다이어트 중이에요":
                    ingredientScores.compute("종합비타민", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("단백질", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "구내염이 자주 생겨요":
                    ingredientScores.compute("비타민B2", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
            }
        }
    }

    /**
     * 가족력에 대한 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateFamilyHistoryScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] familyHistories = response.getResponseText().split(",");
        for (String history : familyHistories) {
            switch (history.trim()) {
                case "간 질환이 있어요":
                    ingredientScores.compute("밀크씨슬", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "혈관 질환이 있어요":
                    ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("코엔자임Q10", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "뼈·관절 질환이 있어요":
                    ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "당뇨가 있어요":
                    ingredientScores.compute("크롬", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("식이섬유", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
            }
        }
    }

    /**
     * 여성건강에 대한 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateFemaleHealthScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] femaleHealths = response.getResponseText().split(",");
        for (String health : femaleHealths) {
            switch (health.trim()) {
                case "임신, 수유 중이에요":
                    ingredientScores.compute("엽산", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("철분", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "생리전 증후군, 유방 통증이 있어요":
                    ingredientScores.compute("감마리놀렌산(GLA)", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "요로감염, 잔뇨감과 같은 비뇨기계 질환이 있거나 걱정돼요":
                    ingredientScores.compute("크랜베리 추출물", (k, v) -> (v == null) ? 5 : v + 5);
                    break;
                case "생리 전후로 우울하거나 예민해요":
                    ingredientScores.compute("비타민B6", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("마그네슘", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
                case "부정 출혈이 월 1회 이상 나타나요":
                    ingredientScores.compute("철분", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("비타민K", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
            }
        }
    }

    /**
     * 남성건강에 대한 점수를 계산합니다.
     *
     * @param response 회원의 응답
     * @param ingredientScores 영양 성분 점수 Map
     */
    private void calculateMaleHealthScores(MemberResponse response, Map<String, Integer> ingredientScores) {
        String[] maleHealths = response.getResponseText().split(",");
        for (String health : maleHealths) {
            switch (health.trim()) {
                case "남성 가족 중 비뇨기계 질환이 있어요":
                    ingredientScores.compute("쏘팔메토", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "이유 불문 머리가 빠지고 머리숱이 적어졌어요":
                    ingredientScores.compute("비오틴", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 4 : v + 4);
                    break;
                case "남성 불임에 대한 불안감이 있거나 2세 계획이 지연되고 있어요":
                    ingredientScores.compute("아르기닌", (k, v) -> (v == null) ? 5 : v + 5);
                    ingredientScores.compute("아연", (k, v) -> (v == null) ? 4 : v + 4);
                    ingredientScores.compute("셀레늄", (k, v) -> (v == null) ? 3 : v + 3);
                    break;
            }
        }
    }

    /**
     * 나이와 BMI에 따라 영양 성분 점수를 조정합니다.
     *
     * @param ingredientScores 초기 영양 성분 점수
     * @param age 회원의 나이
     * @param bmi 회원의 BMI
     */
    private void adjustScoresByAgeAndBMI(Map<String, Integer> ingredientScores, int age, double bmi) {
        // 나이에 따른 조정
        if (age > 50) {
            ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 10 : v + 10);
            ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 10 : v + 10);
        }

        // BMI에 따른 조정
        if (bmi < 18.5) {
            ingredientScores.compute("단백질", (k, v) -> (v == null) ? 10 : v + 10);
            ingredientScores.compute("칼슘", (k, v) -> (v == null) ? 10 : v + 10);
            ingredientScores.compute("비타민D", (k, v) -> (v == null) ? 10 : v + 10);
            ingredientScores.compute("철분", (k, v) -> (v == null) ? 10 : v + 10);
            ingredientScores.compute("비타민B군", (k, v) -> (v == null) ? 8 : v + 8);
        } else if (bmi >= 23 && bmi < 25) {
            ingredientScores.compute("식이섬유", (k, v) -> (v == null) ? 8 : v + 8);
            ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 8 : v + 8);
            ingredientScores.compute("크롬", (k, v) -> (v == null) ? 6 : v + 6);
        } else if (bmi >= 25) {
            ingredientScores.compute("식이섬유", (k, v) -> (v == null) ? 10 : v + 10);
            ingredientScores.compute("비타민C", (k, v) -> (v == null) ? 10 : v + 10);
            ingredientScores.compute("크롬", (k, v) -> (v == null) ? 8 : v + 8);
            ingredientScores.compute("오메가-3", (k, v) -> (v == null) ? 8 : v + 8);
        }
    }
}