package yj.AutoTrade.api.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import yj.AutoTrade.api.exchange.dto.ExchangeRateResponseDto;
import yj.AutoTrade.api.exchange.dto.ExchangeRateRowDto;
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
            
            ExchangeRateResponseDto response = exchangeRateApiClient.getUsdKrwExchangeRate();
            
            if (response == null || response.getStatisticSearch() == null) {
                throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_INVALID_RESPONSE);
            }

            List<ExchangeRateRowDto> rows = response.getStatisticSearch().getRows();
            if (rows == null || rows.isEmpty()) {
                log.warn("한국은행 API에서 환율 데이터를 찾을 수 없음, 기본값 사용");
                return getDefaultExchangeRate();
            }

            // 첫 번째 행의 환율 값 사용
            ExchangeRateRowDto rateRow = rows.get(0);
            if (rateRow.getDataValue() == null || rateRow.getDataValue().trim().isEmpty()) {
                log.warn("환율 값이 비어있음, 기본값 사용");
                return getDefaultExchangeRate();
            }

            BigDecimal exchangeRate = new BigDecimal(rateRow.getDataValue().replace(",", ""));
            log.info("실시간 USD/KRW 환율 조회 성공: {} ({})", exchangeRate, rateRow.getTime());
            
            return exchangeRate;

        } catch (ExchangeRateException e) {
            log.error("환율 조회 실패: {}", e.getMessage());
            return getDefaultExchangeRate();
        } catch (Exception e) {
            log.error("환율 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return getDefaultExchangeRate();
        }
    }

    /**
     * 환율 조회 실패시 사용할 기본 환율
     * 최근 평균 환율을 기반으로 설정
     */
    private BigDecimal getDefaultExchangeRate() {
        BigDecimal defaultRate = new BigDecimal("1300.00");
        log.warn("기본 환율 사용: {}", defaultRate);
        return defaultRate;
    }

    /**
     * 캐시를 무시하고 강제로 최신 환율 조회
     */
    public BigDecimal getLatestUsdKrwRate() {
        try {
            log.info("최신 USD/KRW 환율 강제 조회");
            ExchangeRateResponseDto response = exchangeRateApiClient.getUsdKrwExchangeRate();
            
            if (response != null && 
                response.getStatisticSearch() != null && 
                response.getStatisticSearch().getRows() != null && 
                !response.getStatisticSearch().getRows().isEmpty()) {
                
                String rateValue = response.getStatisticSearch().getRows().get(0).getDataValue();
                if (rateValue != null && !rateValue.trim().isEmpty()) {
                    return new BigDecimal(rateValue.replace(",", ""));
                }
            }
            
            return getDefaultExchangeRate();
            
        } catch (Exception e) {
            log.error("최신 환율 조회 실패: {}", e.getMessage());
            return getDefaultExchangeRate();
        }
    }
}