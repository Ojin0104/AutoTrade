package yj.AutoTrade.binance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yj.AutoTrade.binance.dto.BinanceFuturesAccountResponseDto;
import yj.AutoTrade.binance.dto.BinanceFuturesBalanceResponseDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BinanceFuturesApiClientTest {
    @Autowired
    private BinanceFuturesApiClient binanceFuturesApiClient;


    @DisplayName("Binance Futures API : Balance 조회")
    @Test
    void getBalance() throws Exception {
        List<BinanceFuturesBalanceResponseDto> response = binanceFuturesApiClient.getFuturesBalance();

        assertNotNull(response);
        System.out.println("Balance 조회 응답: " + response);
    }

    @DisplayName("Binance Futures API : Account 조회")
    @Test
    void getAccount() throws Exception {
        BinanceFuturesAccountResponseDto response = binanceFuturesApiClient.getFuturesAccount();

        assertNotNull(response);
        System.out.println("Account 조회 응답: " + response);
    }
}
