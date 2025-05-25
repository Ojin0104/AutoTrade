package yj.AutoTrade.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import yj.AutoTrade.binance.dto.BinanceFuturesOrderRequestDto;
import yj.AutoTrade.binance.dto.BinanceFuturesOrderResponseDto;
import yj.AutoTrade.binance.dto.BinanceChangeLeverageRequestDto;
import yj.AutoTrade.binance.dto.BinanceChangeLeverageResponseDto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;


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


    public BinanceFuturesOrderResponseDto createOrder(BinanceFuturesOrderRequestDto requestDto) throws Exception {

        String finalPayload = buildSignedPayload(requestDto);

        return webClient.post()
                .uri("/fapi/v1/order")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-MBX-APIKEY", apiKey)
                .bodyValue(finalPayload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Binance 선물 주문 API 요청 실패: " + errorBody)))
                )
                .bodyToMono(BinanceFuturesOrderResponseDto.class)
                .block();
    }

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
    
}
