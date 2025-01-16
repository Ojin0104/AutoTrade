package yj.AutoTrade.upbit;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import yj.AutoTrade.upbit.dto.UpbitTickerResponseDto;

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
        this.webClient = webClientBuilder.baseUrl(url + "/v1").build();
    }

    public UpbitTickerResponseDto[] getUpbitTicker(String markets) {
        return webClient.get()
                .uri("/ticker?markets=" + markets)
                .retrieve()
                .bodyToMono(UpbitTickerResponseDto[].class)
                .block();  // 동기 요청
    }
}
