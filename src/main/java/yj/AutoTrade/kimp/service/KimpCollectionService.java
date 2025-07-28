package yj.AutoTrade.kimp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yj.AutoTrade.api.binance.BinanceApiClient;
import yj.AutoTrade.api.binance.dto.BinanceTickerPriceDto;
import yj.AutoTrade.kimp.entity.CollectionStatus;
import yj.AutoTrade.kimp.entity.CoinPair;
import yj.AutoTrade.kimp.entity.KimpHistory;
import yj.AutoTrade.kimp.repository.CoinPairRepository;
import yj.AutoTrade.kimp.repository.KimpHistoryRepository;
import yj.AutoTrade.api.upbit.UpbitApiClient;
import yj.AutoTrade.api.upbit.dto.UpbitTickerResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KimpCollectionService {

    private final KimpHistoryRepository kimpHistoryRepository;
    private final CoinPairRepository coinPairRepository;
    private final UpbitApiClient upbitApiClient;
    private final BinanceApiClient binanceApiClient;

    public void collectAllKimpData() {
        log.info("김프 데이터 수집 시작");
        
        // 활성화되고 배치가 Y인 코인 페어만 조회
        List<CoinPair> batchEnabledPairs = coinPairRepository.findByIsActiveTrueAndBatchEnabled("Y");
        log.info("배치 수집 대상 코인 페어: {} 개", batchEnabledPairs.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (CoinPair pair : batchEnabledPairs) {
            try {
                KimpHistory kimpData = collectKimpData(pair);
                kimpHistoryRepository.save(kimpData);
                successCount++;
                log.debug("김프 수집 성공: {} -> {}, 김프: {}%", 
                         pair.getUpbitSymbol(), pair.getBinanceSymbol(), kimpData.getKimpRate());
            } catch (Exception e) {
                log.error("김프 수집 실패: {} -> {}, 오류: {}", 
                         pair.getUpbitSymbol(), pair.getBinanceSymbol(), e.getMessage());
                saveErrorRecord(pair, e.getMessage());
                failCount++;
            }
        }
        
        log.info("김프 데이터 수집 완료: 성공 {} 개, 실패 {} 개", successCount, failCount);
    }

    private KimpHistory collectKimpData(CoinPair pair) {
        LocalDateTime collectedAt = LocalDateTime.now();
        
        try {
            // 1. USDT-KRW 환율 조회
            BigDecimal usdtKrwRate = getUsdtKrwRate();
            
            // 2. 업비트 가격 조회
            UpbitTickerResponseDto[] upbitTickers = upbitApiClient.getUpbitTicker(pair.getUpbitSymbol());
            if (upbitTickers == null || upbitTickers.length == 0) {
                throw new RuntimeException("업비트 가격 조회 실패");
            }
            
            BigDecimal upbitPrice = new BigDecimal(upbitTickers[0].getTradePrice());
            BigDecimal upbitVolume = new BigDecimal(upbitTickers[0].getAccTradePrice24h());
            
            // 3. 바이낸스 가격 조회
            BinanceTickerPriceDto binanceTicker = binanceApiClient.getTickerPrice(pair.getBinanceSymbol());
            if (binanceTicker == null) {
                throw new RuntimeException("바이낸스 가격 조회 실패");
            }
            
            BigDecimal binancePrice = new BigDecimal(binanceTicker.getPrice());
            
            // 4. 바이낸스 24시간 거래량 조회 (별도 API 필요시)
            BigDecimal binanceVolume = getBinanceVolume24h(pair.getBinanceSymbol());
            
            // 5. 바이낸스 가격을 KRW로 변환
            BigDecimal binancePriceKrw = binancePrice.multiply(usdtKrwRate);
            
            // 6. 김프 계산: ((업비트가격 - 바이낸스가격KRW) / 바이낸스가격KRW) * 100
            BigDecimal priceDifference = upbitPrice.subtract(binancePriceKrw);
            BigDecimal kimpRate = priceDifference
                    .multiply(new BigDecimal("100"))
                    .divide(binancePriceKrw, 4, RoundingMode.HALF_UP);
            
            return KimpHistory.builder()
                    .upbitSymbol(pair.getUpbitSymbol())
                    .binanceSymbol(pair.getBinanceSymbol())
                    .upbitPrice(upbitPrice)
                    .binancePrice(binancePrice)
                    .binancePriceKrw(binancePriceKrw)
                    .usdtKrwRate(usdtKrwRate)
                    .kimpRate(kimpRate)
                    .priceDifference(priceDifference)
                    .upbitVolume24h(upbitVolume)
                    .binanceVolume24h(binanceVolume)
                    .collectedAt(collectedAt)
                    .status(CollectionStatus.SUCCESS)
                    .build();
                    
        } catch (Exception e) {
            log.error("김프 데이터 수집 중 오류: {}", e.getMessage());
            throw e;
        }
    }

    private BigDecimal getUsdtKrwRate() {
        try {
            UpbitTickerResponseDto[] usdtTickers = upbitApiClient.getUpbitTicker("KRW-USDT");
            if (usdtTickers == null || usdtTickers.length == 0) {
                throw new RuntimeException("USDT-KRW 환율 조회 실패");
            }
            return new BigDecimal(usdtTickers[0].getTradePrice());
        } catch (Exception e) {
            log.error("USDT-KRW 환율 조회 실패: {}", e.getMessage());
            throw new RuntimeException("USDT-KRW 환율 조회 실패", e);
        }
    }

    private BigDecimal getBinanceVolume24h(String symbol) {
        // 바이낸스 24시간 통계 API 호출 (ticker/24hr)
        // 현재는 기본값 반환, 필요시 별도 API 구현
        return BigDecimal.ZERO;
    }

    private void saveErrorRecord(CoinPair pair, String errorMessage) {
        try {
            KimpHistory errorRecord = KimpHistory.builder()
                    .upbitSymbol(pair.getUpbitSymbol())
                    .binanceSymbol(pair.getBinanceSymbol())
                    .upbitPrice(BigDecimal.ZERO)
                    .binancePrice(BigDecimal.ZERO)
                    .binancePriceKrw(BigDecimal.ZERO)
                    .usdtKrwRate(BigDecimal.ZERO)
                    .kimpRate(BigDecimal.ZERO)
                    .priceDifference(BigDecimal.ZERO)
                    .upbitVolume24h(BigDecimal.ZERO)
                    .binanceVolume24h(BigDecimal.ZERO)
                    .collectedAt(LocalDateTime.now())
                    .status(CollectionStatus.CALCULATION_ERROR)
                    .errorMessage(errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage)
                    .build();
            
            kimpHistoryRepository.save(errorRecord);
        } catch (Exception e) {
            log.error("오류 레코드 저장 실패: {}", e.getMessage());
        }
    }
}