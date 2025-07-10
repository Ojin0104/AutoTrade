package yj.AutoTrade.kimp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yj.AutoTrade.kimp.entity.CoinPair;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinPairRepository extends JpaRepository<CoinPair, Long> {

    List<CoinPair> findByIsActiveTrue();
    
    List<CoinPair> findByIsActiveTrueAndBatchEnabled(String batchEnabled);
    
    Optional<CoinPair> findByUpbitSymbolAndBinanceSymbol(String upbitSymbol, String binanceSymbol);
    
    List<CoinPair> findByCoinName(String coinName);
    
    boolean existsByUpbitSymbolAndBinanceSymbol(String upbitSymbol, String binanceSymbol);
}