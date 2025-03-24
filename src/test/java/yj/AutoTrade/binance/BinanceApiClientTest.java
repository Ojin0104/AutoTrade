package yj.AutoTrade.binance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yj.AutoTrade.upbit.UpbitApiClient;
import yj.AutoTrade.upbit.dto.UpbitTickerResponseDto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BinanceApiClientTest {

    @Autowired
    private BinanceApiClient binanceApiClient;

    @DisplayName("Binance API : 핑 체크")
    @Test
    void checkBinancePing() {
        // Given
        String marketCode = "KRW-BTC";  // 실제 코인 코드

        // When
        boolean response = binanceApiClient.checkPing();

        // Then
        assertNotNull(response);
        //assertTrue(response.contains("KRW-BTC"));
        System.out.println("Binance API Response: " + response);
        }


    }
