package com.javalab.student.repository.cartOrder;

import com.javalab.student.entity.cartOrder.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 주문(Order) 엔티티에 대한 데이터 접근 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공합니다.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // 기본 CRUD 메서드들이 JpaRepository에 의해 자동으로 제공됩니다.

    /**
     * 특정 사용자의 주문 목록을 최신순으로 조회합니다.
     * @param email 사용자 이메일
     * @param pageable 페이징 정보
     * @return 주문 목록
     */
    @Query("SELECT o FROM Order o WHERE o.member.email = :email ORDER BY o.orderDate DESC")
    List<Order> findOrders(@Param("email") String email, Pageable pageable);

    /**
     * 특정 사용자의 총 주문 수를 조회합니다.
     * @param email 사용자 이메일
     * @return 총 주문 수
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.member.email = :email")
    Long countOrder(@Param("email") String email);

    /**
     * 특정 사용자의 주문 목록을 최신순으로 조회 (페이징 처리) - Pageable 사용 버전 (추가)
     * - 기존 findOrders() 메소드 수정 대신, 페이징 처리를 위한 새로운 메소드 추가
     * @param email 사용자 이메일
     * @param pageable 페이징 정보
     * @return 페이징된 주문 목록
     */
    @Query("SELECT o FROM Order o WHERE o.member.email = :email ORDER BY o.orderDate DESC, o.id desc")
    Page<Order> findOrdersPageable(@Param("email") String email, Pageable pageable);
}
