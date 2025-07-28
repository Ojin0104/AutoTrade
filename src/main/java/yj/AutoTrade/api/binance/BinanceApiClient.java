package yj.AutoTrade.api.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import yj.AutoTrade.api.binance.dto.BinanceErrorResponse;
import yj.AutoTrade.api.binance.dto.BinanceOrderRequestDto;
import yj.AutoTrade.api.binance.dto.BinanceOrderResponseDto;
import yj.AutoTrade.api.binance.dto.BinanceTickerPriceDto;
import yj.AutoTrade.exception.ErrorCode;

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


    public BinanceOrderResponseDto createOrder(BinanceOrderRequestDto requestDto) {

        Map<String, Object> rawMap = objectMapper.convertValue(requestDto, new TypeReference<Map<String, Object>>() {});
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
                        clientResponse.bodyToMono(BinanceErrorResponse.class)
                                .flatMap(error -> {
                                    String code = String.valueOf(error.getCode());
                                    String msg = error.getMsg();
                                    return Mono.error(new BinanceException(ErrorCode.BINANCE_API_ERROR));
                                })
                )
                .bodyToMono(BinanceOrderResponseDto.class)
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

    private String hmacSha256(String data, String key)  {
        try{
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new BinanceException(ErrorCode.BINANCE_API_ERROR, e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 단일 심볼의 현재가 조회
     */
    public BinanceTickerPriceDto getTickerPrice(String symbol) {
        return webClient.get()
                .uri("/api/v3/ticker/price?symbol=" + symbol)
                .retrieve()
                .bodyToMono(BinanceTickerPriceDto.class)
                .block();
    }

    /**
     * 여러 심볼의 현재가 조회
     */
    public BinanceTickerPriceDto[] getTickerPrices(String[] symbols) {
        String symbolsParam = String.join(",", symbols);
        return webClient.get()
                .uri("/api/v3/ticker/price?symbols=[" + symbolsParam + "]")
                .retrieve()
                .bodyToMono(BinanceTickerPriceDto[].class)
                .block();
    }

    /**
     * 모든 심볼의 현재가 조회
     */
    public BinanceTickerPriceDto[] getAllTickerPrices() {
        return webClient.get()
                .uri("/api/v3/ticker/price")
                .retrieve()
                .bodyToMono(BinanceTickerPriceDto[].class)
                .block();
    }

}
