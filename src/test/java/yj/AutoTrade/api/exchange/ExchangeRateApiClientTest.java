package yj.AutoTrade.api.exchange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yj.AutoTrade.api.exchange.dto.KoreaEximExchangeRateDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExchangeRateApiClientTest {

    @Autowired
    private ExchangeRateApiClient exchangeRateApiClient;

    @DisplayName("Exchange API : 실시간 환율 조회")
    @Test
    void testKoreaEximExchangeRateApi() {
        assertDoesNotThrow(() -> {
            List<KoreaEximExchangeRateDto> rates = exchangeRateApiClient.getUsdKrwExchangeRate();
            
            assertNotNull(rates);
            assertFalse(rates.isEmpty());
            
            // USD 환율 찾기
            KoreaEximExchangeRateDto usdRate = rates.stream()
                    .filter(rate -> "USD".equals(rate.getCurUnit()))
                    .findFirst()
                    .orElse(null);
                    
            assertNotNull(usdRate);
            assertNotNull(usdRate.getDealBasR());
            assertFalse(usdRate.getDealBasR().isEmpty());
            
            System.out.println("USD 환율 조회 성공: " + usdRate.getDealBasR());
            System.out.println("통화명: " + usdRate.getCurNm());
        });
    }
}