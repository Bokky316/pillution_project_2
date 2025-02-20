package com.javalab.student.controller.cartOrder;

import com.javalab.student.dto.cartOrder.*;
import com.javalab.student.service.cartOrder.CartService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 장바구니 관련 API 컨트롤러
 * - 장바구니 담기, 목록 조회, 수정, 삭제, 주문 기능 제공
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private static final int MAX_QUANTITY = 10; // 최대 주문 가능 수량

    /**
     * 장바구니에 여러 상품 추가
     * @param cartItemDtos 장바구니 아이템 정보 목록
     * @param principal 사용자 정보
     * @param bindingResult 유효성 검사 결과
     * @return 추가된 장바구니 아이템 ID 목록
     */
    @PostMapping("/add-multiple")
    public ResponseEntity<?> addMultipleToCart(@RequestBody @Valid List<CartItemDto> cartItemDtos, Principal principal, BindingResult bindingResult) {
        log.info("장바구니 상품 추가 요청 - 상품 정보: {}", cartItemDtos);
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError error : fieldErrors) {
                log.error("유효성 검사 실패 - 필드: {}, 메시지: {}", error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body("유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            List<Long> cartItemIds = new ArrayList<>();
            for (CartItemDto cartItemDto : cartItemDtos) {
                log.debug("처리 중인 상품 ID: {}, 수량: {}", cartItemDto.getProductId(), cartItemDto.getQuantity());
                if (!cartService.checkStock(cartItemDto.getProductId(), cartItemDto.getQuantity())) {
                    return ResponseEntity.badRequest().body("재고가 부족합니다. 상품 ID: " + cartItemDto.getProductId());
                }
                Long cartItemId = cartService.addCart(cartItemDto, principal.getName());
                cartItemIds.add(cartItemId);
                log.info("장바구니 상품 추가 완료 - 카트 아이템 ID: {}", cartItemId);
            }
            return ResponseEntity.ok(cartItemIds);
        } catch (EntityNotFoundException e) {
            log.error("상품을 찾을 수 없음", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("상품을 찾을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("장바구니 상품 추가 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 상품 추가에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 장바구니에 상품 추가
     * @param cartItemDto 장바구니 아이템 정보
     * @param principal 사용자 정보
     * @param bindingResult 유효성 검사 결과
     * @return 추가된 장바구니 아이템 ID
     */
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody @Valid CartItemDto cartItemDto, Principal principal, BindingResult bindingResult) {
        log.info("장바구니 상품 추가 요청 - 상품 정보: {}", cartItemDto);

        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError error : fieldErrors) {
                log.error("유효성 검사 실패 - 필드: {}, 메시지: {}", error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body("유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            log.debug("cartItemDto.getProductId(): {}", cartItemDto.getProductId());
            log.debug("cartItemDto.getQuantity(): {}", cartItemDto.getQuantity());

            if (!cartService.checkStock(cartItemDto.getProductId(), cartItemDto.getQuantity())) {
                return ResponseEntity.badRequest().body("재고가 부족합니다.");
            }
            Long cartItemId = cartService.addCart(cartItemDto, principal.getName());
            log.info("장바구니 상품 추가 완료 - 카트 아이템 ID: {}", cartItemId);
            return ResponseEntity.ok(cartItemId);
        } catch (Exception e) {
            log.error("장바구니 상품 추가 실패", e);
            log.error("에러 상세 내용", e); // 상세 에러 로깅 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 상품 추가에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 장바구니 목록 조회
     * @param principal 사용자 정보
     * @return 장바구니 아이템 목록
     */
    @GetMapping
    public ResponseEntity<List<CartDetailDto>> getCartList(Principal principal) {
        log.info("장바구니 목록 조회 요청 - 사용자: {}", principal.getName());
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());
        return ResponseEntity.ok(cartDetailList);
    }

    /**
     * 장바구니 상품 수량 수정
     * @param cartItemId 장바구니 아이템 ID
     * @param requestBody 요청 바디 (수정할 수량 정보)
     * @param principal 사용자 정보
     * @return 수정된 장바구니 아이템 ID
     */
    @CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.PATCH})
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable("cartItemId") Long cartItemId,
            @RequestBody Map<String, Integer> requestBody, // 요청 바디에서 count 를 받음
            Principal principal) {
        log.info("PATCH 요청 수신: cartItemId={}, requestBody={}", cartItemId, requestBody);
        int count = requestBody.get("count");

        log.info("장바구니 상품 수량 수정 요청 - 카트 아이템 ID: {}, 수량: {}", cartItemId, count);
        if (count <= 0 || count > MAX_QUANTITY) {
            return ResponseEntity.badRequest().body("유효하지 않은 수량입니다. 1에서 " + MAX_QUANTITY + " 사이의 값을 입력해주세요.");
        }
        if (!cartService.validateCartItem(cartItemId, principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 장바구니 아이템에 접근 권한이 없습니다.");
        }
        try {
            Long productId = cartService.getItemIdByCartItemId(cartItemId);
            if (!cartService.checkStock(productId, count)) {
                return ResponseEntity.badRequest().body("재고가 부족합니다.");
            }
            cartService.updateCartItemCount(cartItemId, count);
            log.info("장바구니 상품 수량 수정 완료 - 카트 아이템 ID: {}", cartItemId);
            return ResponseEntity.ok(cartItemId);
        } catch (Exception e) {
            log.error("장바구니 상품 수량 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 상품 수량 수정에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 장바구니 상품 삭제
     * @param cartItemId 장바구니 아이템 ID
     * @param principal 사용자 정보
     * @return 삭제된 장바구니 아이템 ID
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal) {
        log.info("장바구니 상품 삭제 요청 - 카트 아이템 ID: {}", cartItemId);
        if (!cartService.validateCartItem(cartItemId, principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 장바구니 아이템에 접근 권한이 없습니다.");
        }
        try {
            cartService.deleteCartItem(cartItemId);
            log.info("장바구니 상품 삭제 완료 - 카트 아이템 ID: {}", cartItemId);
            return ResponseEntity.ok(cartItemId);
        } catch (Exception e) {
            log.error("장바구니 상품 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 상품 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 특정 장바구니 아이템 조회
     * @param cartItemId 장바구니 아이템 ID
     * @param principal 사용자 정보
     * @return 장바구니 아이템 상세 정보
     */
    @GetMapping("/{cartItemId}")
    public ResponseEntity<?> getCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal) {
        log.info("장바구니 아이템 조회 요청 - 카트 아이템 ID: {}", cartItemId);
        if (!cartService.validateCartItem(cartItemId, principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 장바구니 아이템에 접근 권한이 없습니다.");
        }
        try {
            CartDetailDto cartItem = cartService.getCartItemDetail(cartItemId);
            return ResponseEntity.ok(cartItem);
        } catch (EntityNotFoundException e) {
            log.error("장바구니 아이템을 찾을 수 없음", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("장바구니 아이템을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("장바구니 아이템 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("장바구니 아이템 조회에 실패했습니다: " + e.getMessage());
        }
    }
}
