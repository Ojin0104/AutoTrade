package yj.AutoTrade.binance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import yj.AutoTrade.binance.dto.BinanceFuturesAccountResponseDto;
import yj.AutoTrade.binance.dto.BinanceFuturesBalanceResponseDto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    public boolean checkPing(){
        try {
            webClient.get()
                    .uri("/fapi/v3/ping")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();  // 동기 요청
        }catch(Exception e){
            return false;
        }
        return true;
    }


    public List<BinanceFuturesBalanceResponseDto> getFuturesBalance() throws Exception {
        // 1. 요청 파라미터 구성
        Map<String, String> params = new LinkedHashMap<>();
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("recvWindow", "5000");

        // 2. 쿼리 문자열 생성
        String queryString = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        // 3. 서명 생성
        String signature = hmacSha256(queryString, secretKey);

        // 4. 최종 쿼리 스트링
        String finalQuery = queryString + "&signature=" + signature;

        // 5. 요청 전송
        return webClient.get()
                .uri("/fapi/v3/balance?" + finalQuery)
                .header("X-MBX-APIKEY", apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new RuntimeException("잔고 조회 실패: " + errorBody)))
                )
                .bodyToMono(new ParameterizedTypeReference<List<BinanceFuturesBalanceResponseDto>>() {})
                .block();
    }

    public BinanceFuturesAccountResponseDto getFuturesAccount() throws Exception {
        // 1. 요청 파라미터 구성
        Map<String, String> params = new LinkedHashMap<>();
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("recvWindow", "5000");

        // 2. 쿼리 문자열 생성
        String queryString = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        // 3. 서명 생성
        String signature = hmacSha256(queryString, secretKey);

        // 4. 최종 쿼리 스트링
        String finalQuery = queryString + "&signature=" + signature;

        // 5. 요청 전송
        return webClient.get()
                .uri("/fapi/v3/account?" + finalQuery)
                .header("X-MBX-APIKEY", apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new RuntimeException("잔고 조회 실패: " + errorBody)))
                )
                .bodyToMono(new ParameterizedTypeReference<BinanceFuturesAccountResponseDto>() {})
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
