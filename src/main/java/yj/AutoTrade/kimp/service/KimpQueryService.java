package yj.AutoTrade.kimp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yj.AutoTrade.kimp.dto.KimpDataDto;
import yj.AutoTrade.kimp.dto.KimpStatisticsDto;
import yj.AutoTrade.kimp.entity.CoinPair;
import yj.AutoTrade.kimp.entity.KimpHistory;
import yj.AutoTrade.kimp.repository.CoinPairRepository;
import yj.AutoTrade.kimp.repository.KimpHistoryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class KimpQueryService {

    private final KimpHistoryRepository kimpHistoryRepository;
    private final CoinPairRepository coinPairRepository;
    private final KimpCollectionService kimpCollectionService;

    public List<KimpDataDto> getLatestKimpForAllCoins() {
        List<KimpHistory> latestData = kimpHistoryRepository.findLatestKimpForAllCoins();
        Map<String, String> coinNameMap = getCoinNameMap();
        
        return latestData.stream()
                .map(history -> KimpDataDto.from(history, 
                        coinNameMap.get(history.getUpbitSymbol() + "-" + history.getBinanceSymbol())))
                .collect(Collectors.toList());
    }

    public KimpDataDto getLatestKimpForCoin(String upbitSymbol, String binanceSymbol) {
        KimpHistory latest = kimpHistoryRepository
                .findTopByUpbitSymbolAndBinanceSymbolOrderByCollectedAtDesc(upbitSymbol, binanceSymbol)
                .orElseThrow(() -> new RuntimeException("김프 데이터를 찾을 수 없습니다: " + upbitSymbol + " -> " + binanceSymbol));
        
        String coinName = getCoinName(upbitSymbol, binanceSymbol);
        return KimpDataDto.from(latest, coinName);
    }

    public List<KimpDataDto> getKimpHistory(String upbitSymbol, String binanceSymbol, 
                                          LocalDateTime startTime, LocalDateTime endTime) {
        List<KimpHistory> histories = kimpHistoryRepository
                .findByUpbitSymbolAndBinanceSymbolAndCollectedAtBetweenOrderByCollectedAtDesc(
                        upbitSymbol, binanceSymbol, startTime, endTime);
        
        String coinName = getCoinName(upbitSymbol, binanceSymbol);
        return histories.stream()
                .map(history -> KimpDataDto.from(history, coinName))
                .collect(Collectors.toList());
    }

    public List<KimpDataDto> getRecentKimpData(String upbitSymbol, String binanceSymbol) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<KimpHistory> recentData = kimpHistoryRepository
                .findRecentData(upbitSymbol, binanceSymbol, since);
        
        String coinName = getCoinName(upbitSymbol, binanceSymbol);
        return recentData.stream()
                .map(history -> KimpDataDto.from(history, coinName))
                .collect(Collectors.toList());
    }

    public List<KimpDataDto> getProfitableKimp(String upbitSymbol, String binanceSymbol, 
                                             BigDecimal minKimp, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<KimpHistory> profitableData = kimpHistoryRepository
                .findByKimpRange(upbitSymbol, binanceSymbol, minKimp, since);
        
        String coinName = getCoinName(upbitSymbol, binanceSymbol);
        return profitableData.stream()
                .map(history -> KimpDataDto.from(history, coinName))
                .collect(Collectors.toList());
    }

    public KimpStatisticsDto getKimpStatistics(String upbitSymbol, String binanceSymbol, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        
        // 통계 데이터 조회
        Object[] stats = kimpHistoryRepository.getKimpStatistics(upbitSymbol, binanceSymbol, since);
        
        // 전체 데이터 조회 (변동성 계산용)
        List<KimpHistory> allData = kimpHistoryRepository
                .findRecentData(upbitSymbol, binanceSymbol, since);
        
        if (stats[0] == null || allData.isEmpty()) {
            return KimpStatisticsDto.builder()
                    .upbitSymbol(upbitSymbol)
                    .binanceSymbol(binanceSymbol)
                    .averageKimp(BigDecimal.ZERO)
                    .maxKimp(BigDecimal.ZERO)
                    .minKimp(BigDecimal.ZERO)
                    .volatility(BigDecimal.ZERO)
                    .dataCount(0L)
                    .periodStart(since)
                    .periodEnd(LocalDateTime.now())
                    .profitableCount(0L)
                    .profitableRatio(BigDecimal.ZERO)
                    .build();
        }
        
        BigDecimal avgKimp = (BigDecimal) stats[0];
        BigDecimal maxKimp = (BigDecimal) stats[1];
        BigDecimal minKimp = (BigDecimal) stats[2];
        
        // 변동성 계산 (표준편차)
        BigDecimal volatility = calculateVolatility(allData, avgKimp);
        
        // 수익성 데이터 계산 (김프 3% 이상)
        long profitableCount = allData.stream()
                .mapToLong(data -> data.isProfitable(new BigDecimal("3.0")) ? 1L : 0L)
                .sum();
        
        BigDecimal profitableRatio = allData.size() > 0 
                ? new BigDecimal(profitableCount)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(allData.size()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        return KimpStatisticsDto.builder()
                .upbitSymbol(upbitSymbol)
                .binanceSymbol(binanceSymbol)
                .averageKimp(avgKimp)
                .maxKimp(maxKimp)
                .minKimp(minKimp)
                .volatility(volatility)
                .dataCount((long) allData.size())
                .periodStart(since)
                .periodEnd(LocalDateTime.now())
                .profitableCount(profitableCount)
                .profitableRatio(profitableRatio)
                .build();
    }

    public List<CoinPair> getActiveCoinPairs() {
        return coinPairRepository.findByIsActiveTrue();
    }

    @Transactional
    public void collectKimpDataManually() {
        log.info("수동 김프 데이터 수집 시작");
        kimpCollectionService.collectAllKimpData();
    }

    private Map<String, String> getCoinNameMap() {
        return coinPairRepository.findByIsActiveTrue().stream()
                .collect(Collectors.toMap(
                        pair -> pair.getUpbitSymbol() + "-" + pair.getBinanceSymbol(),
                        CoinPair::getCoinName
                ));
    }

    private String getCoinName(String upbitSymbol, String binanceSymbol) {
        return coinPairRepository.findByUpbitSymbolAndBinanceSymbol(upbitSymbol, binanceSymbol)
                .map(CoinPair::getCoinName)
                .orElse("Unknown");
    }

    private BigDecimal calculateVolatility(List<KimpHistory> data, BigDecimal average) {
        if (data.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sumSquaredDiff = data.stream()
                .map(KimpHistory::getKimpRate)
                .map(rate -> rate.subtract(average).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal variance = sumSquaredDiff.divide(new BigDecimal(data.size() - 1), 4, RoundingMode.HALF_UP);
        
        // 제곱근 계산 (간단한 Newton's method)
        return sqrt(variance);
    }

    private BigDecimal sqrt(BigDecimal value) {
        if (value.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal x = value;
        BigDecimal prev;
        
        do {
            prev = x;
            x = x.add(value.divide(x, 4, RoundingMode.HALF_UP)).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
        } while (x.subtract(prev).abs().compareTo(new BigDecimal("0.0001")) > 0);
        
        return x;
    }
}