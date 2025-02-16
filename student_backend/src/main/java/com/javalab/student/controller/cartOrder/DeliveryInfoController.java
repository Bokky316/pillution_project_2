package com.javalab.student.controller.cartOrder;

import com.javalab.student.dto.cartOrder.DeliveryInfoDto;
import com.javalab.student.service.cartOrder.DeliveryInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * 배송 정보 Controller
 */
@RestController
@RequestMapping("/api/delivery-info")
@RequiredArgsConstructor
@Slf4j
public class DeliveryInfoController {

    private final DeliveryInfoService deliveryInfoService;

    /**
     * 배송 정보 생성 API
     *
     * @param deliveryInfoCreateDto 배송 정보 생성 DTO
     * @param principal             현재 사용자 정보
     * @return 생성된 배송 정보 DTO와 201 Created 상태 코드
     */
    @PostMapping
    public ResponseEntity<DeliveryInfoDto> createDeliveryInfo(
            @RequestBody DeliveryInfoDto deliveryInfoCreateDto,
            Principal principal) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            DeliveryInfoDto deliveryInfoDto = deliveryInfoService.createDeliveryInfo(memberId, deliveryInfoCreateDto);
            return new ResponseEntity<>(deliveryInfoDto, HttpStatus.CREATED);
        } catch (NumberFormatException e) {
            log.error("잘못된 회원 ID 형식입니다.", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("배송 정보 생성 중 오류가 발생했습니다.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 배송 정보 조회 API
     *
     * @param id 배송 정보 ID
     * @return 배송 정보 DTO와 200 OK 상태 코드
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryInfoDto> getDeliveryInfo(@PathVariable Long id) {
        try {
            DeliveryInfoDto deliveryInfoDto = deliveryInfoService.getDeliveryInfo(id);
            return new ResponseEntity<>(deliveryInfoDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error("배송 정보 조회 중 오류가 발생했습니다.", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 회원 ID로 배송 정보 목록 조회 API
     *
     * @param principal 현재 사용자 정보
     * @return 배송 정보 DTO 목록과 200 OK 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<DeliveryInfoDto>> getDeliveryInfoByMemberId(Principal principal) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            List<DeliveryInfoDto> deliveryInfoDtos = deliveryInfoService.getDeliveryInfoByMemberId(memberId);
            return new ResponseEntity<>(deliveryInfoDtos, HttpStatus.OK);
        } catch (NumberFormatException e) {
            log.error("잘못된 회원 ID 형식입니다.", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("배송 정보 목록 조회 중 오류가 발생했습니다.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 배송 정보 수정 API
     *
     * @param id                    배송 정보 ID
     * @param deliveryInfoCreateDto 수정할 배송 정보 DTO
     * @return 수정된 배송 정보 DTO와 200 OK 상태 코드
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryInfoDto> updateDeliveryInfo(
            @PathVariable Long id,
            @RequestBody DeliveryInfoDto deliveryInfoCreateDto) {
        try {
            DeliveryInfoDto deliveryInfoDto = deliveryInfoService.updateDeliveryInfo(id, deliveryInfoCreateDto);
            return new ResponseEntity<>(deliveryInfoDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error("배송 정보 수정 중 오류가 발생했습니다.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 배송 정보 삭제 API
     *
     * @param id 배송 정보 ID
     * @return 204 No Content 상태 코드
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeliveryInfo(@PathVariable Long id) {
        try {
            deliveryInfoService.deleteDeliveryInfo(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("배송 정보 삭제 중 오류가 발생했습니다.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
