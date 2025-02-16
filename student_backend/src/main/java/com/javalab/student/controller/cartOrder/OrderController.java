package com.javalab.student.controller.cartOrder;

import com.javalab.student.dto.PageRequestDTO;
import com.javalab.student.dto.cartOrder.AdminOrderDto;
import com.javalab.student.dto.cartOrder.OrderDto;
import com.javalab.student.dto.cartOrder.OrderHistDto;
import com.javalab.student.service.cartOrder.OrderService;
import com.javalab.student.util.PageRequestDTOUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

/**
 * 주문 관련 API 컨트롤러
 * - 주문 생성, 주문 내역 조회, 주문 취소 기능 제공
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     * @param orderDto 주문 정보
     * @param principal 사용자 정보
     * @return 주문 ID
     */
    @PostMapping
    public ResponseEntity<Object> order(@RequestBody @Valid OrderDto orderDto, Principal principal) {
        log.info("주문 생성 요청 - 주문 정보: {}", orderDto);
        try {
            Long orderId = orderService.order(orderDto, principal.getName());
            log.info("주문 생성 완료 - 주문 ID: {}", orderId);
            return ResponseEntity.ok(orderId);
        } catch (Exception e) {
            log.error("주문 생성 실패", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 주문 내역 조회
     * @param page 페이지 번호
     * @param principal 사용자 정보
     * @return 주문 내역 목록
     */
    @GetMapping
    public ResponseEntity<Page<OrderHistDto>> orderHist(@RequestParam(value = "page", defaultValue = "0") int page, Principal principal) {
        log.info("주문 내역 조회 요청 - 사용자: {}, 페이지: {}", principal.getName(), page);
        Pageable pageable = PageRequest.of(page, 4);
        Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(principal.getName(), pageable);
        return ResponseEntity.ok(ordersHistDtoList);
    }

    /**
     * 주문 취소
     * @param orderId 주문 ID
     * @param principal 사용자 정보
     * @return 취소된 주문 ID
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Object> cancelOrder(@PathVariable("orderId") Long orderId, Principal principal) {
        log.info("주문 취소 요청 - 주문 ID: {}", orderId);
        if (!orderService.validateOrder(orderId, principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("주문 취소 권한이 없습니다.");
        }
        orderService.cancelOrder(orderId);
        log.info("주문 취소 완료 - 주문 ID: {}", orderId);
        return ResponseEntity.ok(orderId);
    }

    /**
     * 주문 내역 조회 (페이징 처리)
     * @param pageRequestDTO 페이징 요청 정보
     * @param principal 사용자 정보
     * @return 페이징된 주문 내역 목록
     */
    @GetMapping("/page") // 새로운 엔드포인트 추가
    public ResponseEntity<Page<OrderHistDto>> orderListPage(PageRequestDTO pageRequestDTO, Principal principal) {
        log.info("주문 내역 조회 요청 (페이징) - 사용자: {}, 페이지: {}, 페이지 크기: {}", principal.getName(), pageRequestDTO.getPage(), pageRequestDTO.getSize());
        Pageable pageable = PageRequestDTOUtil.getPageable(pageRequestDTO); // PageRequestDTOUtil 사용
        Page<OrderHistDto> ordersHistDtoPage = orderService.getOrderListPage(principal.getName(), pageable); // 새로운 서비스 메소드 호출
        return ResponseEntity.ok(ordersHistDtoPage);
    }

    /**
     * 관리자용 주문 목록 조회 엔드포인트
     * URL 예: GET /api/orders/admin?page=0&size=10
     */
    @GetMapping("/admin")
    public ResponseEntity<Page<AdminOrderDto>> getAdminOrderItemList(PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageRequestDTOUtil.getPageable(pageRequestDTO);
        Page<AdminOrderDto> adminOrderItemPage = orderService.getAdminOrderItemList(pageable);
        return ResponseEntity.ok(adminOrderItemPage);
    }
    /**
     * 주문 취소 (관리자용) - 새로운 API 엔드포인트 "추가" (기존 코드 수정 X)
     * - 관리자 권한으로 주문을 취소하는 API 엔드포인트 (기존 API 엔드포인트와 별도로 추가)
     *
     * @param orderId 주문 ID
     * @return 취소된 주문 ID
     */
    @PostMapping("/admin/{orderId}/cancel") // ✅ 새로운 API 엔드포인트 URL: /api/admin/orders/{orderId}/cancel (Admin 용)
    public ResponseEntity<Object> cancelOrderAdminByAdmin(@PathVariable("orderId") Long orderId) { // ✅ 새로운 메소드 이름: cancelOrderAdminByAdmin (Admin 용)
        log.info("관리자 주문 취소 요청 - 주문 ID: {}", orderId);
        orderService.cancelOrderAdmin(orderId); // ✅ 새로운 서비스 메소드 호출: cancelOrderAdmin (Admin 용)
        log.info("관리자 주문 취소 완료 - 주문 ID: {}", orderId);
        return ResponseEntity.ok(orderId);
    }

}
