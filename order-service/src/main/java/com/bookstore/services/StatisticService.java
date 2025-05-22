package com.bookstore.services;

import java.time.LocalDate;
import java.util.Map;

public interface StatisticService {
    Map<String, Object> getDailyStats(LocalDate date);

    Map<String, Object> getMonthlyStats(int year, int month);

    Map<String, Object> getYearlyStats(int year);

    Map<String, Object> getCustomRangeStats(LocalDate startDate, LocalDate endDate);
}
