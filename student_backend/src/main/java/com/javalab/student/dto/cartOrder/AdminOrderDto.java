package com.javalab.student.dto.cartOrder;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDto {
    // DataGrid의 각 행(row)은 주문아이템의 고유 id를 기준으로 함.
    private Long id;
    // 원래 주문 번호 (Order.id)
    private Long orderId;
    // 주문자 이름 (예, Order.member.name)
    private String memberName;
    // 단일 주문상품의 이름
    private String productName;
    // 해당 주문상품의 수량
    private Integer quantity;
    // 해당 주문상품의 총액 (orderPrice × count)
    private BigDecimal totalPrice;
    // 주문일자 (yyyy-MM-dd HH:mm)
    private String orderDate;
    // 배송주소 (예: addr + addrDetail + (zipcode))
    private String shippingAddress;
    // 결제 수단 (Payment.paymentMethod)
    private String paymentMethod;
    // 주문 상태 필드 추가
    private String orderStatus;
}