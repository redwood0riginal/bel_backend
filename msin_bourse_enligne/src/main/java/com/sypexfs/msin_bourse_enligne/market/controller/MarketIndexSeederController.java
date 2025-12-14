package com.sypexfs.msin_bourse_enligne.market.controller;

import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import com.sypexfs.msin_bourse_enligne.market.service.MarketIndexSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/market/seed")
@RequiredArgsConstructor
public class MarketIndexSeederController {

    private final MarketIndexSeederService seederService;

    // Seed MASI intraday data for today
    @PostMapping("/masi/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedMasiToday(
            @RequestParam(defaultValue = "5") int intervalMinutes) {
        
        LocalDateTime today = LocalDateTime.now();
        int count = seederService.seedMasiIntradayData(today, intervalMinutes);
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", today.toLocalDate());
        result.put("dataPointsCreated", count);
        result.put("intervalMinutes", intervalMinutes);
        
        return ResponseEntity.ok(ApiResponse.success(result, "MASI intraday data seeded successfully"));
    }

    // Seed MASI intraday data for a specific date
    @PostMapping("/masi")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedMasiForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "5") int intervalMinutes) {
        
        int count = seederService.seedMasiIntradayData(date, intervalMinutes);
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", date.toLocalDate());
        result.put("dataPointsCreated", count);
        result.put("intervalMinutes", intervalMinutes);
        
        return ResponseEntity.ok(ApiResponse.success(result, "MASI intraday data seeded successfully"));
    }

    // Seed MASI data for a date range
    @PostMapping("/masi/range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedMasiForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "5") int intervalMinutes) {
        
        int count = seederService.seedMasiDataForDateRange(startDate, endDate, intervalMinutes);
        
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate.toLocalDate());
        result.put("endDate", endDate.toLocalDate());
        result.put("totalDataPointsCreated", count);
        result.put("intervalMinutes", intervalMinutes);
        
        return ResponseEntity.ok(ApiResponse.success(result, "MASI data range seeded successfully"));
    }

    // Seed all major indices for today
    @PostMapping("/all-indices/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedAllIndicesToday() {
        LocalDateTime today = LocalDateTime.now();
        int count = seederService.seedAllIndicesForDate(today);
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", today.toLocalDate());
        result.put("indicesCreated", count);
        
        return ResponseEntity.ok(ApiResponse.success(result, "All indices seeded successfully"));
    }

    // Seed all major indices for a specific date
    @PostMapping("/all-indices")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedAllIndicesForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        
        int count = seederService.seedAllIndicesForDate(date);
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", date.toLocalDate());
        result.put("indicesCreated", count);
        
        return ResponseEntity.ok(ApiResponse.success(result, "All indices seeded successfully"));
    }

    // Clear all MASI data
    @DeleteMapping("/masi")
    public ResponseEntity<ApiResponse<String>> clearMasiData() {
        seederService.clearMasiData();
        return ResponseEntity.ok(ApiResponse.success("MASI data cleared", "MASI data cleared successfully"));
    }

    // Clear all index data (use with caution!)
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<String>> clearAllIndexData() {
        seederService.clearAllIndexData();
        return ResponseEntity.ok(ApiResponse.success("All index data cleared", "All index data cleared successfully"));
    }
}
