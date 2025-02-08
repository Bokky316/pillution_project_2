package com.javalab.student.dto;

import com.javalab.student.entity.Subscription;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SubscriptionResponseDto
 * - 구독 정보를 클라이언트에 반환하기 위한 DTO
 */
@Getter
public class SubscriptionResponseDto {
    private Long id;
    private String memberEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextBillingDate;
    private String status;
    private String paymentMethod;
    private String deliveryAddress;
    private List<SubscriptionItemDto> items;
    private List<SubscriptionNextItemDto> nextItems; // 다음 회차 결제 예정 상품들
    private LocalDate lastBillingDate;
    private int currentCycle;

    public SubscriptionResponseDto(Subscription subscription) {
        this.id = subscription.getId();
        this.memberEmail = subscription.getMember().getEmail();
        this.startDate = subscription.getStartDate();
        this.endDate = subscription.getEndDate();
        this.nextBillingDate = subscription.getNextBillingDate();
        this.status = subscription.getStatus();
        this.paymentMethod = subscription.getPaymentMethod();
        this.deliveryAddress = subscription.getDeliveryAddress();
        this.lastBillingDate = subscription.getLastBillingDate();
        this.currentCycle = subscription.getCurrentCycle();

        // ✅ Lazy-Loading 방지: DTO 리스트로 변환
        this.items = subscription.getItems().stream()
                .map(SubscriptionItemDto::new)
                .collect(Collectors.toList());

        this.nextItems = subscription.getNextItems().stream()
                .map(SubscriptionNextItemDto::new)
                .collect(Collectors.toList());
    }
}
