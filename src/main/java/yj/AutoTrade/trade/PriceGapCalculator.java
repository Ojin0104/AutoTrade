package yj.AutoTrade.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yj.AutoTrade.binance.BinanceApiClient;
import yj.AutoTrade.upbit.UpbitApiClient;
import yj.AutoTrade.upbit.UpbitException;
import yj.AutoTrade.upbit.dto.UpbitTickerResponseDto;
import yj.AutoTrade.binance.dto.BinanceTickerPriceDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceGapCalculator {
    private final UpbitApiClient upbitApiClient;
    private final BinanceApiClient binanceApiClient;

    /**
     * 실시간 USD/KRW 환율 조회 (업비트 USDT/KRW 가격 사용)
     * @return USD/KRW 환율
     * @throws UpbitException 업비트 API 오류 시
     */
    private BigDecimal getCurrentExchangeRate() {
        UpbitTickerResponseDto[] usdtTickers = upbitApiClient.getUpbitTicker("KRW-USDT");
        if (usdtTickers == null || usdtTickers.length == 0) {
            throw new RuntimeException("업비트 USDT 가격 정보를 가져올 수 없습니다.");
        }
        
        BigDecimal usdtPrice = new BigDecimal(usdtTickers[0].getTradePrice());
        log.debug("실시간 USD/KRW 환율: {}", usdtPrice);
        return usdtPrice;
    }

    /**
     * 김치프리미엄 계산
     * @param upbitSymbol 업비트 심볼 (예: "KRW-BTC")
     * @param binanceSymbol 바이낸스 심볼 (예: "BTCUSDT")
     * @return 김치프리미엄 (백분율)
     */
    public BigDecimal calculatePremium(String upbitSymbol, String binanceSymbol) {
        try {
            log.info("김치프리미엄 계산 시작 - Upbit: {}, Binance: {}", upbitSymbol, binanceSymbol);

            // 1. 실시간 환율 조회
            BigDecimal exchangeRate = getCurrentExchangeRate();
            log.debug("현재 환율: {} KRW/USD", exchangeRate);

            // 2. 업비트 가격 조회 (KRW)
            UpbitTickerResponseDto[] upbitTickers = upbitApiClient.getUpbitTicker(upbitSymbol);
            if (upbitTickers == null || upbitTickers.length == 0) {
                throw new RuntimeException("업비트 가격 정보를 가져올 수 없습니다: " + upbitSymbol);
            }
            BigDecimal upbitPrice = new BigDecimal(upbitTickers[0].getTradePrice());
            log.debug("업비트 가격: {} KRW", upbitPrice);

            // 3. 바이낸스 가격 조회 (USDT)
            BinanceTickerPriceDto binanceTicker = binanceApiClient.getTickerPrice(binanceSymbol);
            if (binanceTicker == null) {
                throw new RuntimeException("바이낸스 가격 정보를 가져올 수 없습니다: " + binanceSymbol);
            }
            BigDecimal binancePrice = new BigDecimal(binanceTicker.getPrice());
            log.debug("바이낸스 가격: {} USDT", binancePrice);

            // 4. 바이낸스 가격을 KRW로 변환 (실시간 환율 사용)
            BigDecimal binancePriceInKRW = binancePrice.multiply(exchangeRate);
            log.debug("바이낸스 가격 (KRW 변환): {} KRW", binancePriceInKRW);

            // 5. 김치프리미엄 계산: ((업비트가격 - 바이낸스가격) / 바이낸스가격) * 100
            BigDecimal priceDifference = upbitPrice.subtract(binancePriceInKRW);
            BigDecimal premium = priceDifference
                    .multiply(new BigDecimal("100"))
                    .divide(binancePriceInKRW, 2, RoundingMode.HALF_UP);

            log.info("김치프리미엄 계산 완료: {}% (업비트: {} KRW, 바이낸스: {} USDT = {} KRW, 환율: {})", 
                    premium, upbitPrice, binancePrice, binancePriceInKRW, exchangeRate);

            return premium;

        } catch (UpbitException e) {
            log.error("업비트 API 오류로 김치프리미엄 계산 실패 - Upbit: {}, Binance: {}, 오류: {} - {}", 
                    upbitSymbol, binanceSymbol, e.getErrorCode(), e.getErrorMessage());
            throw e;
        } catch (Exception e) {
            log.error("김치프리미엄 계산 중 오류 발생 - Upbit: {}, Binance: {}, 오류: {}", 
                    upbitSymbol, binanceSymbol, e.getMessage(), e);
            throw new RuntimeException("김치프리미엄 계산 실패", e);
        }
    }

    /**
     * 김치프리미엄이 특정 임계값을 초과하는지 확인
     * @param upbitSymbol 업비트 심볼
     * @param binanceSymbol 바이낸스 심볼
     * @param threshold 임계값 (백분율)
     * @return true if premium > threshold
     */
    public boolean isPremiumAboveThreshold(String upbitSymbol, String binanceSymbol, BigDecimal threshold) {
        BigDecimal premium = calculatePremium(upbitSymbol, binanceSymbol);
        boolean isAbove = premium.compareTo(threshold) > 0;
        
        log.info("프리미엄 임계값 확인: {}% > {}% = {}", premium, threshold, isAbove);
        return isAbove;
    }

    /**
     * 김치프리미엄이 특정 범위 내에 있는지 확인
     * @param upbitSymbol 업비트 심볼
     * @param binanceSymbol 바이낸스 심볼
     * @param minThreshold 최소 임계값 (백분율)
     * @param maxThreshold 최대 임계값 (백분율)
     * @return true if minThreshold <= premium <= maxThreshold
     */
    public boolean isPremiumInRange(String upbitSymbol, String binanceSymbol, 
                                   BigDecimal minThreshold, BigDecimal maxThreshold) {
        BigDecimal premium = calculatePremium(upbitSymbol, binanceSymbol);
        boolean isInRange = premium.compareTo(minThreshold) >= 0 && premium.compareTo(maxThreshold) <= 0;
        
        log.info("프리미엄 범위 확인: {}% <= {}% <= {}% = {}", 
                minThreshold, premium, maxThreshold, isInRange);
        return isInRange;
    }
}
