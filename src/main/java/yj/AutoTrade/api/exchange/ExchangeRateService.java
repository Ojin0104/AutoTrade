package yj.AutoTrade.api.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import yj.AutoTrade.api.exchange.dto.KoreaEximExchangeRateDto;
import yj.AutoTrade.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    private final ExchangeRateApiClient exchangeRateApiClient;

    /**
     * 실시간 USD/KRW 환율 조회 (캐시 적용 - 1분)
     * @return USD/KRW 환율
     */
    @Cacheable(value = "exchangeRate", unless = "#result == null")
    public BigDecimal getCurrentUsdKrwRate() {
        try {
            log.info("실시간 USD/KRW 환율 조회 시작");
            
            List<KoreaEximExchangeRateDto> exchangeRates = exchangeRateApiClient.getUsdKrwExchangeRate();
            
            if (exchangeRates == null || exchangeRates.isEmpty()) {
                log.error("한국수출입은행 API 응답이 비어있음");
                throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_INVALID_RESPONSE);
            }
            
            log.debug("환율 API 응답 데이터 수: {}", exchangeRates.size());

            // USD 환율 찾기
            KoreaEximExchangeRateDto usdRate = exchangeRates.stream()
                    .filter(rate -> "USD".equals(rate.getCurUnit()))
                    .findFirst()
                    .orElse(null);
            
            if (usdRate == null) {
                log.error("USD 환율 데이터를 찾을 수 없음");
                throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_DATA_NOT_FOUND);
            }

            if (usdRate.getDealBasR() == null || usdRate.getDealBasR().trim().isEmpty()) {
                log.error("USD 환율 값이 비어있음");
                throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_DATA_EMPTY);
            }

            BigDecimal exchangeRate = new BigDecimal(usdRate.getDealBasR().replace(",", ""));
            log.info("USD/KRW 환율 조회 성공: {} (통화: {})", exchangeRate, usdRate.getCurNm());
            
            return exchangeRate;

        } catch (ExchangeRateException e) {
            log.error("환율 조회 실패: [{}] {}", e.getErrorCode().getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("환율 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_API_ERROR, e);
        }
    }


    /**
     * 캐시를 무시하고 강제로 최신 환율 조회
     */
    public BigDecimal getLatestUsdKrwRate() {
        try {
            log.info("최신 USD/KRW 환율 강제 조회");
            List<KoreaEximExchangeRateDto> exchangeRates = exchangeRateApiClient.getUsdKrwExchangeRate();
            
            if (exchangeRates == null || exchangeRates.isEmpty()) {
                throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_INVALID_RESPONSE);
            }
            
            KoreaEximExchangeRateDto usdRate = exchangeRates.stream()
                    .filter(rate -> "USD".equals(rate.getCurUnit()))
                    .findFirst()
                    .orElse(null);
            
            if (usdRate == null) {
                log.error("최신 환율 조회: USD 환율 데이터를 찾을 수 없음");
                throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_DATA_NOT_FOUND);
            }
                
            String rateValue = usdRate.getDealBasR();
            if (rateValue == null || rateValue.trim().isEmpty()) {
                log.error("최신 환율 조회: USD 환율 값이 비어있음");
                throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_DATA_EMPTY);
            }
            
            BigDecimal exchangeRate = new BigDecimal(rateValue.replace(",", ""));
            log.info("최신 USD/KRW 환율 강제 조회 성공: {}", exchangeRate);
            return exchangeRate;
            
        } catch (ExchangeRateException e) {
            log.error("최신 환율 조회 실패: [{}] {}", e.getErrorCode().getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("최신 환율 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_API_ERROR, e);
        }
    }
}