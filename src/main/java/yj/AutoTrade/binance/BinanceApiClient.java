package yj.AutoTrade.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import yj.AutoTrade.binance.dto.OrderRequestDto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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

    public String createOrder(OrderRequestDto dto) throws Exception {
        Map<String, Object> rawMap = objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {});

        Map<String, String> params = rawMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        e -> e.getKey()
                        ,
                        entry -> entry.getValue() instanceof Enum<?> e ? e.name() : entry.getValue().toString(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        params.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String payload = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        String signature = hmacSha256(payload, secretKey);
        params.put("signature", signature);



        MultiValueMap<String, String> multiValueParams = new LinkedMultiValueMap<>();
        params.forEach(multiValueParams::add);

        return webClient.post()
                .uri("/api/v3/order")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-MBX-APIKEY", apiKey)
                .bodyValue(multiValueParams)  // ⬅️ 쿼리 문자열이 아닌 MultiValueMap 자체!
                .retrieve()
                .bodyToMono(String.class)
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
