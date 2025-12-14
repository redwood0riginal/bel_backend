-- Seed Market Orderbook (Sample buy/sell orders)
-- V8__seed_market_orderbook.sql

INSERT INTO market_schema.market_orderbook 
(symbol, side, quantity, price, order_count, date_order, order_market_id, 
 order_type, is_own_order, delete, delete_all, created_at)
VALUES
-- ATW (Attijariwafa Bank) - Buy Orders
('ATW', 'BUY', 1000, 489.00, 5, CURRENT_TIMESTAMP, 'ORD-ATW-B-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('ATW', 'BUY', 1500, 488.50, 8, CURRENT_TIMESTAMP, 'ORD-ATW-B-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('ATW', 'BUY', 2000, 488.00, 12, CURRENT_TIMESTAMP, 'ORD-ATW-B-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('ATW', 'BUY', 800, 487.50, 4, CURRENT_TIMESTAMP, 'ORD-ATW-B-004', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('ATW', 'BUY', 1200, 487.00, 6, CURRENT_TIMESTAMP, 'ORD-ATW-B-005', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- ATW - Sell Orders
('ATW', 'SELL', 900, 491.00, 4, CURRENT_TIMESTAMP, 'ORD-ATW-S-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('ATW', 'SELL', 1300, 491.50, 7, CURRENT_TIMESTAMP, 'ORD-ATW-S-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('ATW', 'SELL', 1800, 492.00, 10, CURRENT_TIMESTAMP, 'ORD-ATW-S-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('ATW', 'SELL', 700, 492.50, 3, CURRENT_TIMESTAMP, 'ORD-ATW-S-004', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('ATW', 'SELL', 1100, 493.00, 5, CURRENT_TIMESTAMP, 'ORD-ATW-S-005', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- BCP - Buy Orders
('BCP', 'BUY', 1500, 267.50, 8, CURRENT_TIMESTAMP, 'ORD-BCP-B-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('BCP', 'BUY', 2000, 267.00, 11, CURRENT_TIMESTAMP, 'ORD-BCP-B-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('BCP', 'BUY', 1200, 266.50, 6, CURRENT_TIMESTAMP, 'ORD-BCP-B-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('BCP', 'BUY', 900, 266.00, 5, CURRENT_TIMESTAMP, 'ORD-BCP-B-004', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- BCP - Sell Orders
('BCP', 'SELL', 1400, 269.00, 7, CURRENT_TIMESTAMP, 'ORD-BCP-S-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('BCP', 'SELL', 1800, 269.50, 9, CURRENT_TIMESTAMP, 'ORD-BCP-S-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('BCP', 'SELL', 1000, 270.00, 5, CURRENT_TIMESTAMP, 'ORD-BCP-S-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- IAM - Buy Orders
('IAM', 'BUY', 3000, 116.00, 15, CURRENT_TIMESTAMP, 'ORD-IAM-B-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('IAM', 'BUY', 4000, 115.50, 20, CURRENT_TIMESTAMP, 'ORD-IAM-B-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('IAM', 'BUY', 2500, 115.00, 12, CURRENT_TIMESTAMP, 'ORD-IAM-B-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- IAM - Sell Orders
('IAM', 'SELL', 2800, 117.00, 14, CURRENT_TIMESTAMP, 'ORD-IAM-S-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('IAM', 'SELL', 3500, 117.50, 18, CURRENT_TIMESTAMP, 'ORD-IAM-S-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('IAM', 'SELL', 2000, 118.00, 10, CURRENT_TIMESTAMP, 'ORD-IAM-S-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- LBV (Label Vie) - Buy Orders
('LBV', 'BUY', 500, 2875.00, 3, CURRENT_TIMESTAMP, 'ORD-LBV-B-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('LBV', 'BUY', 700, 2870.00, 4, CURRENT_TIMESTAMP, 'ORD-LBV-B-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('LBV', 'BUY', 400, 2865.00, 2, CURRENT_TIMESTAMP, 'ORD-LBV-B-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- LBV - Sell Orders
('LBV', 'SELL', 600, 2885.00, 3, CURRENT_TIMESTAMP, 'ORD-LBV-S-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('LBV', 'SELL', 800, 2890.00, 4, CURRENT_TIMESTAMP, 'ORD-LBV-S-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('LBV', 'SELL', 500, 2895.00, 2, CURRENT_TIMESTAMP, 'ORD-LBV-S-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- WAA (Wafa Assurance) - Buy Orders
('WAA', 'BUY', 150, 3840.00, 2, CURRENT_TIMESTAMP, 'ORD-WAA-B-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('WAA', 'BUY', 200, 3835.00, 3, CURRENT_TIMESTAMP, 'ORD-WAA-B-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('WAA', 'BUY', 100, 3830.00, 1, CURRENT_TIMESTAMP, 'ORD-WAA-B-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- WAA - Sell Orders
('WAA', 'SELL', 180, 3855.00, 2, CURRENT_TIMESTAMP, 'ORD-WAA-S-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('WAA', 'SELL', 220, 3860.00, 3, CURRENT_TIMESTAMP, 'ORD-WAA-S-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('WAA', 'SELL', 120, 3865.00, 1, CURRENT_TIMESTAMP, 'ORD-WAA-S-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- HPS - Buy Orders
('HPS', 'BUY', 100, 4540.00, 2, CURRENT_TIMESTAMP, 'ORD-HPS-B-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('HPS', 'BUY', 150, 4535.00, 3, CURRENT_TIMESTAMP, 'ORD-HPS-B-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('HPS', 'BUY', 80, 4530.00, 1, CURRENT_TIMESTAMP, 'ORD-HPS-B-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- HPS - Sell Orders
('HPS', 'SELL', 120, 4555.00, 2, CURRENT_TIMESTAMP, 'ORD-HPS-S-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('HPS', 'SELL', 170, 4560.00, 3, CURRENT_TIMESTAMP, 'ORD-HPS-S-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('HPS', 'SELL', 90, 4565.00, 1, CURRENT_TIMESTAMP, 'ORD-HPS-S-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- COL (Cosumar) - Buy Orders
('COL', 'BUY', 800, 217.00, 4, CURRENT_TIMESTAMP, 'ORD-COL-B-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('COL', 'BUY', 1000, 216.50, 5, CURRENT_TIMESTAMP, 'ORD-COL-B-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('COL', 'BUY', 600, 216.00, 3, CURRENT_TIMESTAMP, 'ORD-COL-B-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),

-- COL - Sell Orders
('COL', 'SELL', 900, 218.00, 4, CURRENT_TIMESTAMP, 'ORD-COL-S-001', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('COL', 'SELL', 1100, 218.50, 5, CURRENT_TIMESTAMP, 'ORD-COL-S-002', 'LIMIT', false, false, false, CURRENT_TIMESTAMP),
('COL', 'SELL', 700, 219.00, 3, CURRENT_TIMESTAMP, 'ORD-COL-S-003', 'LIMIT', false, false, false, CURRENT_TIMESTAMP);

-- Create unique constraint on order_market_id if not exists
CREATE UNIQUE INDEX IF NOT EXISTS idx_orderbook_market_id_unique ON market_schema.market_orderbook(order_market_id);
