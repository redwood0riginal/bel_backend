package com.sypexfs.msin_bourse_enligne.portfolio.repository;

import com.sypexfs.msin_bourse_enligne.portfolio.entity.PortfolioPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, Long> {
    
    List<PortfolioPosition> findByPortfolioId(Long portfolioId);
    
    Optional<PortfolioPosition> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);
    
    @Query("SELECT p FROM PortfolioPosition p WHERE p.portfolio.userId = :userId")
    List<PortfolioPosition> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT p FROM PortfolioPosition p WHERE p.portfolio.id = :portfolioId ORDER BY p.marketValue DESC")
    List<PortfolioPosition> findByPortfolioIdOrderByMarketValueDesc(@Param("portfolioId") Long portfolioId);
    
    @Query("SELECT COUNT(p) FROM PortfolioPosition p WHERE p.portfolio.id = :portfolioId")
    long countByPortfolioId(@Param("portfolioId") Long portfolioId);
}
