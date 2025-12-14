package com.sypexfs.msin_bourse_enligne.market.simulator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimulatorAutoStarter implements CommandLineRunner, DisposableBean {

    private final MarketPriceSimulatorThread priceSimulator;
    private final OrderbookSimulatorThread orderbookSimulator;
    private final TransactionSimulatorThread transactionSimulator;
    private final IndexSimulatorThread indexSimulator;

    @Override
    public void run(String... args) throws Exception {
        // Simulators disabled - uncomment to auto-start
        log.info("Market simulators auto-start is DISABLED. Use API to start manually.");
        
        /*
        log.info("Auto-starting market simulators...");
        
        if (!priceSimulator.isRunning()) {
            priceSimulator.startSimulator();
            log.info("Price simulator started.");
        }
        
        if (!orderbookSimulator.isRunning()) {
            orderbookSimulator.startSimulator();
            log.info("Orderbook simulator started.");
        }
        
        if (!transactionSimulator.isRunning()) {
            transactionSimulator.startSimulator();
            log.info("Transaction simulator started.");
        }
        
        if (!indexSimulator.isRunning()) {
            indexSimulator.startSimulator();
            log.info("Index simulator started.");
        }
        
        log.info("All market simulators auto-started successfully.");
        */
    }

    @Override
    public void destroy() throws Exception {
        log.info("Stopping market simulators...");
        
        if (priceSimulator.isRunning()) {
            priceSimulator.stopSimulator();
            log.info("Price simulator stopped.");
        }
        
        if (orderbookSimulator.isRunning()) {
            orderbookSimulator.stopSimulator();
            log.info("Orderbook simulator stopped.");
        }
        
        if (transactionSimulator.isRunning()) {
            transactionSimulator.stopSimulator();
            log.info("Transaction simulator stopped.");
        }
        
        if (indexSimulator.isRunning()) {
            indexSimulator.stopSimulator();
            log.info("Index simulator stopped.");
        }
        
        log.info("All market simulators stopped successfully.");
    }
}
