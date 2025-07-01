package yj.AutoTrade.upbit;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import yj.AutoTrade.upbit.dto.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Component
public class UpbitApiClient {
    private final WebClient webClient;
    private final String accessKey;
    private final String secretKey;

    public UpbitApiClient(
            @Value("${upbit.url}") String url,
            @Value("${upbit.access-key}") String accessKey,
            @Value("${upbit.secret-key}") String secretKey,
            WebClient.Builder webClientBuilder
    ) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.webClient = webClientBuilder.baseUrl(url).build();


    }

    private String generateAuthenticationToken() {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .sign(algorithm);

        return "Bearer " + jwtToken;
    }

    private String generateOrderAuthenticationToken(UpbitRequestParamsDto upbitRequestParamsDto) {
        try{
            ArrayList<String> queryElements = new ArrayList<>();
            for(Map.Entry<String, String> entity : upbitRequestParamsDto.toHashMap().entrySet()) {
                queryElements.add(entity.getKey() + "=" + entity.getValue());
            }
            String queryString = String.join("&", queryElements.toArray(new String[0]));

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(queryString.getBytes(StandardCharsets.UTF_8));

            String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            String jwtToken = JWT.create()
                    .withClaim("access_key", accessKey)
                    .withClaim("nonce", UUID.randomUUID().toString())
                    .withClaim("query_hash", queryHash)
                    .withClaim("query_hash_alg", "SHA512")
                    .sign(algorithm);

            return "Bearer " + jwtToken;
        }catch(NoSuchAlgorithmException e){
            throw new UpbitException("wrong hashAlgorithm",e.getMessage());
        }
    }

    public UpbitTickerResponseDto[] getUpbitTicker(String markets) {
        return webClient.get()
                .uri("/v1/ticker?markets=" + markets)
                .retrieve()
                .bodyToMono(UpbitTickerResponseDto[].class)
                .block();  // 동기 요청
    }

    public UpbitAccountResponseDto[] getUpbitAccount(){

        return webClient.get()
                .uri("/v1/accounts")
                .header("Authorization", generateAuthenticationToken())
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(UpbitAccountResponseDto[].class)
                .block();  // 동기 요청
    }

    @Retry(name = "externalApi")
    @CircuitBreaker(name = "upbitApi", fallbackMethod = "fallback")
    public UpbitOrderResponseDto createOrder(UpbitOrderRequestDto upbitOrderRequestDto)  {

       //body 전송형식 변경 필요
        return webClient.post()
                .uri("/v1/orders")
                .header("Authorization", generateOrderAuthenticationToken(upbitOrderRequestDto))
                .header("Content-Type", "application/json")
                .bodyValue(upbitOrderRequestDto.toHashMap()).retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(UpbitErrorResponse.class)
                                .flatMap(error -> {
                                    String errorCode = error.getError().getName();       // 예: "invalid_order"
                                    String errorMessage = error.getError().getMessage(); // 예: "주문 수량이 부족합니다."
                                    return Mono.error(new UpbitException(errorCode, errorMessage));
                                })
                )
                .bodyToMono(UpbitOrderResponseDto.class)
                .block();  // 동기 요청
    }

    private <T> T fallback(Object request, Throwable t) {
        log.error("[CircuitBreaker Fallback] BinanceFuturesApiClient: " + t.getMessage());
        throw new RuntimeException("BinanceFuturesApiClient fallback: " + t.getMessage(), t);
    }
}
