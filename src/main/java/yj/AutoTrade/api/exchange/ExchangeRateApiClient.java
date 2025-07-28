package yj.AutoTrade.api.exchange;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import yj.AutoTrade.api.exchange.dto.ExchangeRateResponseDto;
import yj.AutoTrade.exception.ErrorCode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ExchangeRateApiClient {

    private final WebClient webClient;

    @Value("${exchange.api.key:}")
    private String apiKey;

    public ExchangeRateApiClient(
            @Value("${exchange.api.url:https://ecos.bok.or.kr/api}") String apiUrl,
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    /**
     * 한국은행 Open API에서 USD/KRW 환율 조회
     * @return ExchangeRateResponseDto
     */
    public ExchangeRateResponseDto getUsdKrwExchangeRate() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            String uriPath = String.format("/StatisticSearch/%s/json/kr/1/1/036Y001/DD/%s/%s/0000001", 
                    apiKey, today, today);

            log.debug("한국은행 환율 API 호출: {}", uriPath);
            
            ExchangeRateResponseDto response = webClient.get()
                    .uri(uriPath)
                    .retrieve()
                    .bodyToMono(ExchangeRateResponseDto.class)
                    .block();
            
            if (response != null && response.getStatisticSearch() != null && 
                response.getStatisticSearch().getResult() != null) {
                
                String resultCode = response.getStatisticSearch().getResult().getResultCode();
                if (!"200".equals(resultCode)) {
                    log.error("한국은행 API 오류: {} - {}", 
                            resultCode, 
                            response.getStatisticSearch().getResult().getResultMessage());
                    throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_API_ERROR);
                }
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("환율 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_API_ERROR, e);
        }
    }

    /**
     * 대체 환율 API 호출 (exchangerate-api.com 사용)
     * API 키 없이 사용 가능한 무료 서비스
     */
    public ExchangeRateResponseDto getUsdKrwExchangeRateFromFallback() {
        try {
            log.debug("대체 환율 API 호출: exchangerate-api.com");
            
            // 이 부분은 실제로는 다른 응답 구조를 가지므로 필요시 별도 DTO 생성 필요
            // 현재는 한국은행 API 우선 사용
            throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_SERVICE_UNAVAILABLE);
            
        } catch (Exception e) {
            log.error("대체 환율 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new ExchangeRateException(ErrorCode.EXCHANGE_RATE_FALLBACK_FAILED, e);
        }
    }
}