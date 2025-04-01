package yj.AutoTrade.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import yj.AutoTrade.binance.dto.BinanceOrderRequestDto;
import yj.AutoTrade.binance.dto.BinanceOrderResponseDto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Component
public class BinanceApiClient {
    private final WebClient webClient;
    private final String apiKey;
    private final String secretKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BinanceApiClient(
            @Value("${binance.url}") String url,
            @Value("${binance.api-key}") String apiKey,
            @Value("${binance.secret-key}") String secretKey,
            WebClient.Builder webClientBuilder
    ) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.webClient = webClientBuilder.baseUrl(url).build();
    }


    public boolean checkPing(){

        try {
            webClient.get()
                    .uri("/api/v3/ping")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();  // 동기 요청
        }catch(Exception e){
            return false;
        }
        return true;
    }


    public BinanceOrderResponseDto createOrder(BinanceOrderRequestDto dto) throws Exception {

        Map<String, Object> rawMap = objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {});
        Map<String, String> params = rawMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey
                        ,
                        entry -> entry.getValue() instanceof Enum<?> e ? e.name() : entry.getValue().toString(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        params.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String payload = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String signature = hmacSha256(payload, secretKey);
        String finalPayload = payload + "&signature=" + signature;

        return webClient.post()
                .uri("/api/v3/order")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-MBX-APIKEY", apiKey)
                .bodyValue(finalPayload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Binance 주문 API 요청 실패: " + errorBody)))
                )
                .bodyToMono(BinanceOrderResponseDto.class)
                .block();
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
