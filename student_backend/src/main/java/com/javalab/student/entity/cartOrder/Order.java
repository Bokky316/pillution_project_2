package com.javalab.student.entity.cartOrder;

import com.javalab.student.constant.OrderStatus;
import com.javalab.student.dto.cartOrder.OrderDto;
import com.javalab.student.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import com.javalab.student.entity.Member;
import com.javalab.student.entity.cartOrder.Address;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 정보를 담는 엔티티 클래스.
 * 주문과 관련된 기본 정보, 주문 상태, 배송 정보 등을 관리합니다.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "orderItems")
public class Order extends BaseEntity {

    /** 주문 ID, Primary Key */
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 주문을 한 회원, ManyToOne 관계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /** 주문 날짜 */
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime orderDate;

    /** 주문 상태 */
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    /** 주문 총액 */
    @Column(name = "order_amount")
    private BigDecimal amount;

    /** 운송장 번호 */
    @Column(name = "waybill_num")
    private String waybillNum;

    /** 택배사 코드 */
    @Column(name = "parcel_cd")
    private String parcelCd;

    /** 주문 아이템 목록, OneToMany 관계 */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL
            , orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @BatchSize(size = 10) // OrderItem을 즉시 로딩할 때, 지정된 크기만큼 미리 로딩합니다.
    private List<OrderItem> orderItems = new ArrayList<>();

    /** 배송 주소, OneToOne 관계 */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;

    /** 결제 정보, OneToOne 관계 */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    /**
     * 주문 아이템 추가 메소드
     *
     * @param orderItem 추가할 주문 아이템
     */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    /**
     * 주문 생성 메소드
     *
     * @param member 주문 회원
     * @param orderItemList 주문 아이템 리스트
     * @return 생성된 주문 객체
     */
    public static Order createOrder(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMember(member);    // 주문자 정보 세팅

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 주문Item 갯수만큼 주문Item에 추가
        for (OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getOrderPrice().multiply(BigDecimal.valueOf(orderItem.getCount())));
        }
        // 총 주문금액 세팅
        order.setAmount(totalAmount);
        // 주문상태 세팅(ORDR01 : 주문)
        order.setOrderStatus(OrderStatus.ORDERED);

        // 주문일자를 오늘날짜로 세팅
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    /**
     * 주문 총 가격 계산 메소드
     *
     * @return 주문 총 가격
     */
    public BigDecimal getTotalPrice() {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 주문 취소 메소드
     */
    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCELED;
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    /**
     * Entity를 DTO로 변환하는 메소드
     *
     * @return OrderDto
     */
    public OrderDto entityToDto() {
        return OrderDto.builder()
                .id(this.id)
                .memberId(this.member.getId())
                .orderDate(this.orderDate)
                .orderStatus(this.orderStatus)
                .amount(this.amount)
                .waybillNum(this.waybillNum)
                .parcelCd(this.parcelCd)
                .build();
    }

    public void cancelOrderAdmin() {
        this.orderStatus = OrderStatus.CANCELED;
        for (OrderItem orderItem : orderItems) {
            orderItem.cancelAndAddStock(); // 새로운 메서드 사용
        }
    }
}
