package com.javalab.student.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    /**
     * IllegalStateException 예외 처리기 (주문 취소 실패, 재고 부족 등)
     * - 특정 비즈니스 로직 실패에 대한 예외 처리
     * - 400 Bad Request 상태 코드와 함께 오류 메시지 반환
     */
    @ExceptionHandler(IllegalStateException.class) // ✅ 추가된 부분
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) { // ✅ 추가된 부분
        log.warn("IllegalStateException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage()); // 클라이언트 에러 (400)
    }

}
