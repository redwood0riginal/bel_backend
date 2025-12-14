-- Seed Market Instruments (Casablanca Stock Exchange stocks)
-- V3__seed_market_instruments.sql

INSERT INTO market_schema.market_instruments 
(market_place, symbol, name, class_id, issuer, market_segment, price_type, match_type, 
 trading_type, trading_status, currency, sector, market_type, created_at, updated_at)
VALUES
-- Banking Sector
('CSE', 'ATW', 'ATTIJARIWAFA BANK', 'EQ', 'Attijariwafa Bank', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Banking', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'BCP', 'BANQUE CENTRALE POPULAIRE', 'EQ', 'BCP', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Banking', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'CDM', 'CREDIT DU MAROC', 'EQ', 'Crédit du Maroc', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Banking', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'CIH', 'CIH BANK', 'EQ', 'CIH Bank', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Banking', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'BCI', 'BMCI', 'EQ', 'BMCI', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Banking', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Telecom Sector
('CSE', 'IAM', 'ITISSALAT AL-MAGHRIB', 'EQ', 'Maroc Telecom', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Telecommunications', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Real Estate Sector
('CSE', 'ADH', 'DOUJA PROM ADDOHA', 'EQ', 'Douja Promotion', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Real Estate', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'ALM', 'ALLIANCES', 'EQ', 'Alliances', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Real Estate', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Insurance Sector
('CSE', 'WAA', 'WAFA ASSURANCE', 'EQ', 'Wafa Assurance', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Insurance', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'SAH', 'SAHAM ASSURANCE', 'EQ', 'Saham Assurance', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Insurance', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Mining Sector
('CSE', 'CMT', 'CIMENTS DU MAROC', 'EQ', 'Ciments du Maroc', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Building Materials', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'LAF', 'LAFARGEHOLCIM MAR', 'EQ', 'LafargeHolcim Maroc', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Building Materials', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'MNG', 'MANAGEM', 'EQ', 'Managem', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Mining', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Retail & Distribution
('CSE', 'LBV', 'LABEL VIE', 'EQ', 'Label Vie', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Retail', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'MAR', 'MARJANE HOLDING', 'EQ', 'Marjane Holding', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Retail', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Energy Sector
('CSE', 'SID', 'SONASID', 'EQ', 'Sonasid', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Steel', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'TAQ', 'TAQA MOROCCO', 'EQ', 'Taqa Morocco', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Energy', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Agribusiness
('CSE', 'COL', 'COSUMAR', 'EQ', 'Cosumar', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Agribusiness', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'LES', 'LESIEUR CRISTAL', 'EQ', 'Lesieur Cristal', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Agribusiness', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Automotive
('CSE', 'AUT', 'AUTO HALL', 'EQ', 'Auto Hall', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Automotive', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Pharmaceuticals
('CSE', 'SBM', 'SOTHEMA', 'EQ', 'Sothema', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Pharmaceuticals', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Transport & Logistics
('CSE', 'TMA', 'TIMAR', 'EQ', 'Timar', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Transport', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Technology
('CSE', 'HPS', 'HPS', 'EQ', 'HPS', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Technology', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Holdings
('CSE', 'SNI', 'SOCIETE NATIONALE D INVESTISSEMENT', 'EQ', 'SNI', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Holdings', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
('CSE', 'DLM', 'DELTA HOLDING', 'EQ', 'Delta Holding', 'MAIN', 'PRICE', 'CONTINUOUS', 
 'NORMAL', 'ACTIVE', 'MAD', 'Holdings', 'EQUITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Create unique constraint on symbol if not exists
CREATE UNIQUE INDEX IF NOT EXISTS idx_instruments_symbol_unique ON market_schema.market_instruments(symbol);
