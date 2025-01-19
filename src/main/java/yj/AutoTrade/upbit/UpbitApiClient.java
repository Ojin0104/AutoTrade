package yj.AutoTrade.upbit;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import yj.AutoTrade.upbit.dto.UpbitAccountResponseDto;
import yj.AutoTrade.upbit.dto.UpbitTickerResponseDto;

import java.util.UUID;

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
}
