package com.sypexfs.msin_bourse_enligne.portfolio.repository;

import com.sypexfs.msin_bourse_enligne.portfolio.entity.PortfolioSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioSummaryRepository extends JpaRepository<PortfolioSummary, Long> {
    
    Optional<PortfolioSummary> findByPortfolioId(Long portfolioId);
    
    @Query("SELECT ps FROM PortfolioSummary ps WHERE ps.portfolio.userId = :userId")
    Optional<PortfolioSummary> findByUserId(@Param("userId") Long userId);
    
    void deleteByPortfolioId(Long portfolioId);
}
