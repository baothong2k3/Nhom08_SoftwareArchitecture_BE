package com.bookstore.repositories;

import com.bookstore.entities.Order;
import com.bookstore.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findAllByStatusOrderByCreatedAtDesc(@Param("status") OrderStatus status);

    List<Order> findAllByStatus(OrderStatus status);

    List<Order> findAllByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND YEAR(o.createdAt) = :year")
    List<Order> findAllByStatusAndYear(@Param("status") OrderStatus status, @Param("year") int year);
}