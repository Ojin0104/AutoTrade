package yj.AutoTrade.binance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import yj.AutoTrade.upbit.dto.UpbitAccountResponseDto;

@Component
public class BinanceApiClient {
    private final WebClient webClient;
    private final String accessKey;
    private final String secretKey;

    public BinanceApiClient(
            @Value("https://api.binance.com") String url,
            @Value("${upbit.access-key}") String accessKey,
            @Value("${upbit.secret-key}") String secretKey,
            WebClient.Builder webClientBuilder
    ) {
        this.accessKey = accessKey;
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

}
