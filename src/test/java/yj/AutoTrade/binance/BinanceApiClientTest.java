package yj.AutoTrade.binance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yj.AutoTrade.api.binance.BinanceApiClient;
import yj.AutoTrade.api.binance.dto.*;


import java.math.BigDecimal;

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


    @DisplayName("Binance API : 주문 요청(limit)")
    @Test
    void limitOrder_shouldSucceed() throws Exception {
        BinanceOrderRequestDto request = BinanceOrderRequestDto.builder()
                .symbol("BTCUSDT")
                .side(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .timeInForce(TimeInForce.GTC)
                .quantity(new BigDecimal("0.001"))
                .price(new BigDecimal("20000.00"))
                .newClientOrderId("test-limit-" + System.currentTimeMillis())
                .newOrderRespType(NewOrderRespType.FULL)
                .recvWindow(5000L)
                .timestamp(System.currentTimeMillis())
                .build();

        BinanceOrderResponseDto response = binanceApiClient.createOrder(request);

        assertNotNull(response);
        System.out.println("LIMIT 주문 응답: " + response);
    }

    }


