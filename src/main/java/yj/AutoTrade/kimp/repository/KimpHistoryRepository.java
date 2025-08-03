package yj.AutoTrade.kimp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yj.AutoTrade.kimp.entity.KimpHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KimpHistoryRepository extends JpaRepository<KimpHistory, Long> {

    // 최신 김프 데이터 조회
    Optional<KimpHistory> findTopByUpbitSymbolAndBinanceSymbolOrderByCollectedAtDesc(
            String upbitSymbol, String binanceSymbol);

    // 특정 기간 김프 데이터 조회
    List<KimpHistory> findByUpbitSymbolAndBinanceSymbolAndCollectedAtBetweenOrderByCollectedAtDesc(
            String upbitSymbol, String binanceSymbol, LocalDateTime startTime, LocalDateTime endTime);

    // 최근 24시간 데이터
    @Query("SELECT k FROM KimpHistory k WHERE k.upbitSymbol = :upbitSymbol AND k.binanceSymbol = :binanceSymbol " +
           "AND k.collectedAt >= :since ORDER BY k.collectedAt DESC")
    List<KimpHistory> findRecentData(@Param("upbitSymbol") String upbitSymbol, 
                                   @Param("binanceSymbol") String binanceSymbol,
                                   @Param("since") LocalDateTime since);

    // 김프율 범위로 검색
    @Query("SELECT k FROM KimpHistory k WHERE k.upbitSymbol = :upbitSymbol AND k.binanceSymbol = :binanceSymbol " +
           "AND ABS(k.kimpRate) >= :minKimp AND k.collectedAt >= :since ORDER BY k.collectedAt DESC")
    List<KimpHistory> findByKimpRange(@Param("upbitSymbol") String upbitSymbol,
                                    @Param("binanceSymbol") String binanceSymbol,
                                    @Param("minKimp") java.math.BigDecimal minKimp,
                                    @Param("since") LocalDateTime since);

    // 김프 통계
    @Query("SELECT AVG(k.kimpRate), MAX(k.kimpRate), MIN(k.kimpRate) FROM KimpHistory k " +
           "WHERE k.upbitSymbol = :upbitSymbol AND k.binanceSymbol = :binanceSymbol " +
           "AND k.collectedAt >= :since")
    Object[] getKimpStatistics(@Param("upbitSymbol") String upbitSymbol,
                             @Param("binanceSymbol") String binanceSymbol,
                             @Param("since") LocalDateTime since);

    // 모든 코인의 최신 김프
    @Query("SELECT k FROM KimpHistory k WHERE k.id IN " +
           "(SELECT MAX(k2.id) FROM KimpHistory k2 GROUP BY k2.upbitSymbol, k2.binanceSymbol)")
    List<KimpHistory> findLatestKimpForAllCoins();
}