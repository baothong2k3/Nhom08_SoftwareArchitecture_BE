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
import com.bookstore.services.StatisticService;
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
    private StatisticService statisticService;

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> stats = statisticService.getDailyStats(date);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStats(
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> stats = statisticService.getMonthlyStats(year, month);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> getYearlyStats(
            @RequestParam int year) {
        Map<String, Object> stats = statisticService.getYearlyStats(year);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> getCustomRangeStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = statisticService.getCustomRangeStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
