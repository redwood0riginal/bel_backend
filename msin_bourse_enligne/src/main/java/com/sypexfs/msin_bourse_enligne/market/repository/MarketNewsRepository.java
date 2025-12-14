package com.sypexfs.msin_bourse_enligne.market.repository;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketNews;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MarketNewsRepository extends JpaRepository<MarketNews, Long> {

    @Query("SELECT n FROM MarketNews n ORDER BY n.publishedAt DESC")
    List<MarketNews> findAllOrderByPublishedAtDesc(Pageable pageable);

    @Query("SELECT n FROM MarketNews n WHERE n.publishedAt >= :startDate ORDER BY n.publishedAt DESC")
    List<MarketNews> findRecentNews(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT n FROM MarketNews n WHERE n.urgency = :urgency ORDER BY n.publishedAt DESC")
    List<MarketNews> findByUrgency(@Param("urgency") String urgency, Pageable pageable);

    @Query("SELECT n FROM MarketNews n WHERE LOWER(n.headline) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY n.publishedAt DESC")
    List<MarketNews> searchByHeadline(@Param("keyword") String keyword, Pageable pageable);
}
