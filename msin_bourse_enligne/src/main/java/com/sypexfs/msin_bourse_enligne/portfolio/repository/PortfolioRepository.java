package com.sypexfs.msin_bourse_enligne.portfolio.repository;

import com.sypexfs.msin_bourse_enligne.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    List<Portfolio> findByUserId(Long userId);
    
    Optional<Portfolio> findByUserIdAndStatus(Long userId, String status);
    
    Optional<Portfolio> findByAccountNumber(String accountNumber);
    
    @Query("SELECT p FROM Portfolio p WHERE p.userId = :userId AND p.status = 'ACTIVE'")
    Optional<Portfolio> findActivePortfolioByUserId(@Param("userId") Long userId);
    
    boolean existsByAccountNumber(String accountNumber);
}
