package yj.AutoTrade.upbit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import yj.AutoTrade.upbit.dto.UpbitAccountResponseDto;
import yj.AutoTrade.upbit.dto.UpbitTickerResponseDto;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@TestPropertySource(properties = {
        "upbit.url=https://api.upbit.com",
        "upbit.access-key=access",
        "upbit.secret-key=secret"
})
class UpbitApiClientTest {
    @Autowired
    private UpbitApiClient upbitApiClient;

    @Test
    void testGetUpbitTicker() {
        // Given
        String marketCode = "KRW-BTC";  // 실제 코인 코드

        // When
        UpbitTickerResponseDto[] response = upbitApiClient.getUpbitTicker(marketCode);

        // Then
        assertNotNull(response);
        //assertTrue(response.contains("KRW-BTC"));
        System.out.println("Upbit API Response: " + response[0]);
    }

    @Test
    void testGetUpbitAccount() {

        UpbitAccountResponseDto[] upbitAccount = upbitApiClient.getUpbitAccount();

        assertNotNull(upbitAccount);

        System.out.println("Upbit API Response: " + upbitAccount[0]);
    }
}