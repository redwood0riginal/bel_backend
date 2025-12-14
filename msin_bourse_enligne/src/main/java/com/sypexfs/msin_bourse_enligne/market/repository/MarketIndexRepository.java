package com.sypexfs.msin_bourse_enligne.market.repository;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketIndexRepository extends JpaRepository<MarketIndex, Long> {

    Optional<MarketIndex> findByCode(String code);

    List<MarketIndex> findByIndexType(String indexType);

    @Query("SELECT i FROM MarketIndex i ORDER BY i.datePrice DESC")
    List<MarketIndex> findAllOrderByDatePriceDesc();

    @Query("SELECT i FROM MarketIndex i WHERE i.code = :code ORDER BY i.datePrice DESC")
    List<MarketIndex> findByCodeOrderByDatePriceDesc(@Param("code") String code);

    @Query("SELECT i FROM MarketIndex i WHERE i.code = :code ORDER BY i.datePrice DESC LIMIT 1")
    Optional<MarketIndex> findLatestByCode(@Param("code") String code);
}
