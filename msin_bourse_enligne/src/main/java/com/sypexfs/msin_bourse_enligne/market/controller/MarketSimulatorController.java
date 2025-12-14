package com.sypexfs.msin_bourse_enligne.market.controller;

import com.sypexfs.msin_bourse_enligne.market.simulator.MarketPriceSimulatorThread;
import com.sypexfs.msin_bourse_enligne.market.simulator.OrderbookSimulatorThread;
import com.sypexfs.msin_bourse_enligne.market.simulator.TransactionSimulatorThread;
import com.sypexfs.msin_bourse_enligne.market.simulator.IndexSimulatorThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to manage market simulators
 */
@RestController
@RequestMapping("/api/market/simulator")
@RequiredArgsConstructor
@Slf4j
public class MarketSimulatorController {

    private final MarketPriceSimulatorThread priceSimulator;
    private final OrderbookSimulatorThread orderbookSimulator;

    private final TransactionSimulatorThread transactionSimulator;
    private final IndexSimulatorThread indexSimulator;

    /**
     * Start all simulators
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startAllSimulators() {
        log.info("Starting all market simulators");
        
        try {
            if (!priceSimulator.isRunning()) {
                priceSimulator.startSimulator();
            }
            
            if (!orderbookSimulator.isRunning()) {
                orderbookSimulator.startSimulator();
            }
            
            if (!transactionSimulator.isRunning()) {
                transactionSimulator.startSimulator();
            }

            if (!indexSimulator.isRunning()) {
                indexSimulator.startSimulator();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All simulators started successfully");
            response.put("status", getSimulatorStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting simulators", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error starting simulators: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Stop all simulators
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopAllSimulators() {
        log.info("Stopping all market simulators");
        
        try {
            priceSimulator.stopSimulator();
            orderbookSimulator.stopSimulator();
            transactionSimulator.stopSimulator();
            indexSimulator.stopSimulator();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All simulators stopped successfully");
            response.put("status", getSimulatorStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error stopping simulators", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error stopping simulators: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Start price simulator only
     */
    @PostMapping("/price/start")
    public ResponseEntity<Map<String, Object>> startPriceSimulator() {
        log.info("Starting price simulator");
        
        if (!priceSimulator.isRunning()) {
            priceSimulator.startSimulator();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Price simulator started");
        response.put("running", priceSimulator.isRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Stop price simulator only
     */
    @PostMapping("/price/stop")
    public ResponseEntity<Map<String, Object>> stopPriceSimulator() {
        log.info("Stopping price simulator");
        
        priceSimulator.stopSimulator();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Price simulator stopped");
        response.put("running", priceSimulator.isRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Start orderbook simulator only
     */
    @PostMapping("/orderbook/start")
    public ResponseEntity<Map<String, Object>> startOrderbookSimulator() {
        log.info("Starting orderbook simulator");
        
        if (!orderbookSimulator.isRunning()) {
            orderbookSimulator.startSimulator();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Orderbook simulator started");
        response.put("running", orderbookSimulator.isRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Stop orderbook simulator only
     */
    @PostMapping("/orderbook/stop")
    public ResponseEntity<Map<String, Object>> stopOrderbookSimulator() {
        log.info("Stopping orderbook simulator");
        
        orderbookSimulator.stopSimulator();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Orderbook simulator stopped");
        response.put("running", orderbookSimulator.isRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Start transaction simulator only
     */
    @PostMapping("/transaction/start")
    public ResponseEntity<Map<String, Object>> startTransactionSimulator() {
        log.info("Starting transaction simulator");
        
        if (!transactionSimulator.isRunning()) {
            transactionSimulator.startSimulator();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Transaction simulator started");
        response.put("running", transactionSimulator.isRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Stop transaction simulator only
     */
    @PostMapping("/transaction/stop")
    public ResponseEntity<Map<String, Object>> stopTransactionSimulator() {
        log.info("Stopping transaction simulator");
        
        transactionSimulator.stopSimulator();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Transaction simulator stopped");
        response.put("running", transactionSimulator.isRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Start index simulator only
     */
    @PostMapping("/index/start")
    public ResponseEntity<Map<String, Object>> startIndexSimulator() {
        log.info("Starting index simulator");
        
        if (!indexSimulator.isRunning()) {
            indexSimulator.startSimulator();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Index simulator started");
        response.put("running", indexSimulator.isRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Stop index simulator only
     */
    @PostMapping("/index/stop")
    public ResponseEntity<Map<String, Object>> stopIndexSimulator() {
        log.info("Stopping index simulator");
        
        indexSimulator.stopSimulator();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Index simulator stopped");
        response.put("running", indexSimulator.isRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get status of all simulators
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", getSimulatorStatus());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to get simulator status
     */
    private Map<String, Boolean> getSimulatorStatus() {
        Map<String, Boolean> status = new HashMap<>();
        status.put("priceSimulator", priceSimulator.isRunning());
        status.put("orderbookSimulator", orderbookSimulator.isRunning());

        status.put("transactionSimulator", transactionSimulator.isRunning());
        status.put("indexSimulator", indexSimulator.isRunning());
        return status;
    }
}
