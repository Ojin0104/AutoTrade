package yj.AutoTrade.kimp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yj.AutoTrade.kimp.entity.CoinPair;
import yj.AutoTrade.kimp.repository.CoinPairRepository;
import yj.AutoTrade.exception.ErrorCode;
import yj.AutoTrade.exception.TradeException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CoinPairService {

    private final CoinPairRepository coinPairRepository;

    public List<CoinPair> getAllCoinPairs() {
        return coinPairRepository.findAll();
    }

    public List<CoinPair> getActiveCoinPairs() {
        return coinPairRepository.findByIsActiveTrue();
    }

    public List<CoinPair> getBatchEnabledCoinPairs() {
        return coinPairRepository.findByIsActiveTrueAndBatchEnabled("Y");
    }

    public CoinPair createCoinPair(CoinPair coinPair) {
        // 중복 체크
        if (coinPairRepository.existsByUpbitSymbolAndBinanceSymbol(
                coinPair.getUpbitSymbol(), coinPair.getBinanceSymbol())) {
            throw new TradeException(ErrorCode.COIN_PAIR_DUPLICATE);
        }

        CoinPair saved = coinPairRepository.save(coinPair);
        log.info("코인 페어 생성: {} -> {}, 배치: {}", 
                saved.getUpbitSymbol(), saved.getBinanceSymbol(), saved.getBatchEnabled());
        return saved;
    }

    public CoinPair updateCoinPair(Long id, CoinPair coinPair) {
        CoinPair existing = coinPairRepository.findById(id)
                .orElseThrow(() -> new TradeException(ErrorCode.COIN_PAIR_NOT_FOUND));

        existing.setUpbitSymbol(coinPair.getUpbitSymbol());
        existing.setBinanceSymbol(coinPair.getBinanceSymbol());
        existing.setCoinName(coinPair.getCoinName());
        existing.setDescription(coinPair.getDescription());

        CoinPair updated = coinPairRepository.save(existing);
        log.info("코인 페어 수정: {} -> {}", updated.getUpbitSymbol(), updated.getBinanceSymbol());
        return updated;
    }

    public void toggleActive(Long id) {
        CoinPair coinPair = coinPairRepository.findById(id)
                .orElseThrow(() -> new TradeException(ErrorCode.COIN_PAIR_NOT_FOUND));

        if (coinPair.getIsActive()) {
            coinPair.deactivate();
        } else {
            coinPair.activate();
        }

        coinPairRepository.save(coinPair);
        log.info("코인 페어 활성화 토글: {} -> {}, 활성화: {}", 
                coinPair.getUpbitSymbol(), coinPair.getBinanceSymbol(), coinPair.getIsActive());
    }

    public void toggleBatch(Long id) {
        CoinPair coinPair = coinPairRepository.findById(id)
                .orElseThrow(() -> new TradeException(ErrorCode.COIN_PAIR_NOT_FOUND));

        if (coinPair.isBatchEnabled()) {
            coinPair.disableBatch();
        } else {
            coinPair.enableBatch();
        }

        coinPairRepository.save(coinPair);
        log.info("코인 페어 배치 토글: {} -> {}, 배치: {}", 
                coinPair.getUpbitSymbol(), coinPair.getBinanceSymbol(), coinPair.getBatchEnabled());
    }

    public void enableBatch(Long id) {
        CoinPair coinPair = coinPairRepository.findById(id)
                .orElseThrow(() -> new TradeException(ErrorCode.COIN_PAIR_NOT_FOUND));

        coinPair.enableBatch();
        coinPairRepository.save(coinPair);
        log.info("코인 페어 배치 활성화: {} -> {}", coinPair.getUpbitSymbol(), coinPair.getBinanceSymbol());
    }

    public void disableBatch(Long id) {
        CoinPair coinPair = coinPairRepository.findById(id)
                .orElseThrow(() -> new TradeException(ErrorCode.COIN_PAIR_NOT_FOUND));

        coinPair.disableBatch();
        coinPairRepository.save(coinPair);
        log.info("코인 페어 배치 비활성화: {} -> {}", coinPair.getUpbitSymbol(), coinPair.getBinanceSymbol());
    }

    public void deleteCoinPair(Long id) {
        CoinPair coinPair = coinPairRepository.findById(id)
                .orElseThrow(() -> new TradeException(ErrorCode.COIN_PAIR_NOT_FOUND));

        coinPairRepository.delete(coinPair);
        log.info("코인 페어 삭제: {} -> {}", coinPair.getUpbitSymbol(), coinPair.getBinanceSymbol());
    }
}