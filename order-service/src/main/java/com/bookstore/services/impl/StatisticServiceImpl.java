package com.bookstore.services.impl;

import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;
import com.bookstore.repositories.OrderRepository;
import com.bookstore.services.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class StatisticServiceImpl implements StatisticService {
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Map<String, Object> getDailyStats(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return getStatsForRange(start, end, false);
    }

    @Override
    public Map<String, Object> getMonthlyStats(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return getStatsForRange(start, end, false);
    }

    @Override
    public Map<String, Object> getYearlyStats(int year) {
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        Map<String, Object> stats = getStatsForRange(start, end, true);

        // Add monthly revenue breakdown
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            List<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, monthStart, monthEnd);
            BigDecimal revenue = orders.stream()
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyRevenue.add(Map.of(
                    "month", month,
                    "revenue", revenue
            ));
        }
        stats.put("monthlyRevenue", monthlyRevenue);
        return stats;
    }

    @Override
    public Map<String, Object> getCustomRangeStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        Map<String, Object> stats = getStatsForRange(start, end, false);

        // Add daily revenue breakdown
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);
            List<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, dayStart, dayEnd);
            BigDecimal revenue = orders.stream()
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyRevenue.add(Map.of(
                    "date", current.toString(),
                    "revenue", revenue
            ));
            current = current.plusDays(1);
        }
        stats.put("dailyRevenue", dailyRevenue);
        return stats;
    }

    private Map<String, Object> getStatsForRange(LocalDateTime start, LocalDateTime end, boolean isYearly) {
        // Fetch delivered orders within the time range
        List<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, start, end);

        // Total books sold
        long totalBooksSold = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .mapToLong(OrderDetail::getQuantity)
                .sum();

        // Number of orders
        long totalOrders = orders.size();

        // Total revenue
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total customers (distinct user IDs)
        long totalCustomers = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .count();

        // Top 5 best-selling books
        Map<Long, Map<String, Object>> bookSales = new HashMap<>();
        orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .forEach(detail -> {
                    Long bookId = detail.getBookId();
                    bookSales.compute(bookId, (id, map) -> {
                        if (map == null) {
                            map = new HashMap<>();
                            map.put("title", detail.getBookTitle());
                            map.put("quantity", 0L);
                        }
                        long quantity = (long) map.get("quantity") + detail.getQuantity();
                        map.put("quantity", quantity);
                        return map;
                    });
                });

        List<Map<String, Object>> topBooks = bookSales.values().stream()
                .sorted((a, b) -> Long.compare((long) b.get("quantity"), (long) a.get("quantity")))
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooksSold", totalBooksSold);
        stats.put("topBooks", topBooks);
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCustomers", totalCustomers);

        return stats;
    }
}
