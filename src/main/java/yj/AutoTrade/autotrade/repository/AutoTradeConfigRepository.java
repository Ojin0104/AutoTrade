package yj.AutoTrade.autotrade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yj.AutoTrade.autotrade.entity.AutoTradeConfig;

import java.util.List;

@Repository
public interface AutoTradeConfigRepository extends JpaRepository<AutoTradeConfig, Long> {

    List<AutoTradeConfig> findByIsActiveTrue();

    @Query("SELECT a FROM AutoTradeConfig a WHERE a.isActive = true AND a.currentDailyTrades < a.maxDailyTrades AND a.currentDailyLoss < a.maxDailyLoss")
    List<AutoTradeConfig> findExecutableConfigs();

    List<AutoTradeConfig> findByUpbitSymbolAndBinanceSymbol(String upbitSymbol, String binanceSymbol);
}