/*
 * @ (#) StatisticController.java    1.0    16/05/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package com.bookstore.controllers;/*
 * @description:
 * @author: Bao Thong
 * @date: 16/05/2025
 * @version: 1.0
 */

import com.bookstore.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistic")
public class StatisticController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/top-books")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingBooks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Map<String, Object>> topBooks = orderService.getTopSellingBooks(startDate, endDate);
        return ResponseEntity.ok(topBooks);
    }

    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue(
            @RequestParam int year) {
        List<Map<String, Object>> monthlyRevenue = orderService.getMonthlyRevenue(year);
        return ResponseEntity.ok(monthlyRevenue);
    }
}
