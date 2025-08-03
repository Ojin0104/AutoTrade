package yj.AutoTrade.api.exchange;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import yj.AutoTrade.api.exchange.dto.KoreaEximExchangeRateDto;
import yj.AutoTrade.exception.ErrorCode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class ExchangeRateApiClient {

    private final WebClient webClient;

    @Value("${exchange.api.key}")
    private String apiKey;

    public ExchangeRateApiClient(
            @Value("${exchange.api.url}") String apiUrl,
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    /**
     * 한국수출입은행 Open API에서 USD/KRW 환율 조회
     * 주말/공휴일인 경우 최근 영업일 데이터 조회
     * @return List<KoreaEximExchangeRateDto>
     */
    public List<KoreaEximExchangeRateDto> getUsdKrwExchangeRate() {
        LocalDate searchDate = LocalDate.now();
        
        // 최대 7일 전까지 시도
        for (int i = 0; i < 7; i++) {
            try {
                String dateStr = searchDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                
                String uriPath = String.format("/site/program/financial/exchangeJSON?authkey=%s&searchdate=%s&data=AP01", 
                        apiKey, dateStr);

                log.debug("한국수출입은행 환율 API 호출: {} (날짜: {})", uriPath, dateStr);
                
                KoreaEximExchangeRateDto[] responseArray = webClient.get()
                        .uri(uriPath)
                        .retrieve()
                        .bodyToMono(KoreaEximExchangeRateDto[].class)
                        .block();
                
                if (responseArray != null && responseArray.length > 0) {
                    log.info("환율 데이터 조회 성공: {} (기준일: {})", responseArray.length, dateStr);
                    return List.of(responseArray);
                }
                
                log.debug("{}일 환율 데이터 없음, 이전 날짜로 재시도", dateStr);
                searchDate = searchDate.minusDays(1);
                
            } catch (Exception e) {
                log.warn("{}일 환율 조회 실패, 이전 날짜로 재시도: {}", searchDate, e.getMessage());
                searchDate = searchDate.minusDays(1);
            }
        }
        
        log.error("최근 7일간 환율 데이터를 찾을 수 없음");
        throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_DATA_NOT_FOUND);
    }

}