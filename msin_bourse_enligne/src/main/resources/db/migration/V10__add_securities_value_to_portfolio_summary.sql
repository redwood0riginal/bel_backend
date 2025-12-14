-- Add securities_value column to portfolio_summary table
ALTER TABLE portfolio_schema.portfolio_summary
ADD COLUMN securities_value DECIMAL(20, 2) DEFAULT 0.00;

-- Update existing records to calculate securities_value from positions
UPDATE portfolio_schema.portfolio_summary ps
SET securities_value = COALESCE(
    (SELECT SUM(market_value)
     FROM portfolio_schema.portfolio_positions pp
     WHERE pp.portfolio_id = ps.portfolio_id),
    0.00
);

-- Add comment to column
COMMENT ON COLUMN portfolio_schema.portfolio_summary.securities_value IS 'Total market value of all securities/positions in the portfolio';
