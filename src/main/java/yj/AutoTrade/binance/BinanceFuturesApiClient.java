package yj.AutoTrade.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import yj.AutoTrade.binance.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Slf4j
@Component
public class BinanceFuturesApiClient {
    private final WebClient webClient;
    private final String apiKey;
    private final String secretKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BinanceFuturesApiClient(
            @Value("${binance.futures-url}") String url,
            @Value("${binance.api-key}") String apiKey,
            @Value("${binance.secret-key}") String secretKey,
            WebClient.Builder webClientBuilder
    ) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    @Retry(name = "externalApi")
    @CircuitBreaker(name = "binanceFuturesApi", fallbackMethod = "fallback")
    public BinanceFuturesOrderResponseDto createOrder(BinanceFuturesOrderRequestDto requestDto) throws Exception {

        String finalPayload = buildSignedPayload(requestDto);

        return webClient.post()
                .uri("/fapi/v1/order")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-MBX-APIKEY", apiKey)
                .bodyValue(finalPayload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(BinanceErrorResponse.class)
                                .flatMap(error -> {
                                    String code = String.valueOf(error.getCode());
                                    String msg = error.getMsg();
                                    return Mono.error(new BinanceException(code, msg));
                                })
                )
                .bodyToMono(BinanceFuturesOrderResponseDto.class)
                .block();
    }

    @CircuitBreaker(name = "binanceFuturesApi", fallbackMethod = "fallback")
    public BinanceChangeLeverageResponseDto changeLeverage(BinanceChangeLeverageRequestDto requestDto) throws Exception  {
        
        String finalPayload = buildSignedPayload(requestDto);

        return webClient.post()
                .uri("/fapi/v1/leverage")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-MBX-APIKEY", apiKey)
                .bodyValue(finalPayload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Binance 선물 주문 레버리지 변경 실패: " + errorBody)))
                )
                .bodyToMono(BinanceChangeLeverageResponseDto.class)
                .block();
    }


    public String buildSignedPayload(Object requestDto) throws Exception {
        Map<String, Object> rawMap = objectMapper.convertValue(requestDto, new TypeReference<>() {});
        
        Map<String, String> params = rawMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() instanceof Enum<?> e ? e.name() : entry.getValue().toString(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        params.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String payload = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String signature = hmacSha256(payload, secretKey);
        return payload + "&signature=" + signature;
    }

    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(rawHmac);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    

    private <T> T fallback(Object request, Throwable t) {
        log.error("[CircuitBreaker Fallback] BinanceFuturesApiClient: " + t.getMessage());
        throw new RuntimeException("BinanceFuturesApiClient fallback: " + t.getMessage(), t);
    }
}
