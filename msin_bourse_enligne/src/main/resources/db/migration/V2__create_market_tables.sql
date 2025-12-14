-- Create market schema tables

-- Market Summaries
CREATE TABLE market_schema.market_summaries (
    id BIGSERIAL PRIMARY KEY,
    sec_id VARCHAR(50),
    market_place VARCHAR(50),
    open_close_indicator VARCHAR(10),
    symbol VARCHAR(20),
    name VARCHAR(255),
    last_closing_price DECIMAL(15,4),
    top DECIMAL(15,4),
    tov DECIMAL(20,2),
    closing_price DECIMAL(15,4),
    opening_price DECIMAL(15,4),
    date_trans TIMESTAMP,
    price DECIMAL(15,4),
    variation DECIMAL(10,4),
    higher_price DECIMAL(15,4),
    lower_price DECIMAL(15,4),
    higher_limit DECIMAL(15,4),
    lower_limit DECIMAL(15,4),
    static_higher_limit DECIMAL(15,4),
    static_lower_limit DECIMAL(15,4),
    vwap DECIMAL(15,4),
    quantity DECIMAL(15,2),
    volume DECIMAL(15,2),
    date_update TIMESTAMP,
    notional_exposer DECIMAL(20,2),
    underlying_ref_price DECIMAL(15,4),
    open_intrest DECIMAL(15,2),
    theoretical_price DECIMAL(15,4),
    auction_qty DECIMAL(15,2),
    auction_imbalance_qty DECIMAL(15,2),
    auction_price DECIMAL(15,4),
    auction_type VARCHAR(50),
    price_band_limit_sup DECIMAL(15,4),
    price_band_limit_inf DECIMAL(15,4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_summaries_symbol ON market_schema.market_summaries(symbol);
CREATE INDEX idx_summaries_sec_id ON market_schema.market_summaries(sec_id);
