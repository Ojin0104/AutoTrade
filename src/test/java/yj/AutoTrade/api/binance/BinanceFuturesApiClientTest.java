package yj.AutoTrade.api.binance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import yj.AutoTrade.api.binance.BinanceFuturesApiClient;
import yj.AutoTrade.api.binance.dto.*;

@SpringBootTest
public class BinanceFuturesApiClientTest {
    @Autowired
    private BinanceFuturesApiClient binanceFuturesApiClient;

  
    @DisplayName("Binance API : 주문 요청(limit)")
    @Test
    void MarketOrder_shouldSucceed() throws Exception {
        BinanceFuturesOrderRequestDto request = BinanceFuturesOrderRequestDto.builder()
                .symbol("BTCUSDT")
                .side(OrderSide.BUY)
                .type(OrderType.MARKET)
                .quantity("0.001")
                .newClientOrderId("test-limit-" + System.currentTimeMillis())
                .newOrderRespType(NewOrderRespType.FULL)
                .recvWindow(5000L)
                .timestamp(System.currentTimeMillis())
                .build();

        BinanceFuturesOrderResponseDto response = binanceFuturesApiClient.createOrder(request);

        assertNotNull(response);
        System.out.println("LIMIT 주문 응답: " + response);
    }

    @DisplayName("Binance API : 레버리지 변경 요청")
    @Test
    void changeLeverage_shouldSucceed() throws Exception {
        BinanceChangeLeverageRequestDto request = BinanceChangeLeverageRequestDto.builder()
                .symbol("BTCUSDT")
                .leverage(10)
                .recvWindow(5000L)
                .timestamp(System.currentTimeMillis())
                .build();

        BinanceChangeLeverageResponseDto response = binanceFuturesApiClient.changeLeverage(request);

        assertNotNull(response);
        System.out.println("레버리지 변경 응답: " + response);
        }
//
//    @DisplayName("Binance Futures API : Balance 조회")
//    @Test
//    void getBalance() throws Exception {
//        List<BinanceFuturesBalanceResponseDto> response = binanceFuturesApiClient.getFuturesBalance();
//
//        assertNotNull(response);
//        System.out.println("Balance 조회 응답: " + response);
//    }
//
//    @DisplayName("Binance Futures API : Account 조회")
//    @Test
//    void getAccount() throws Exception {
//        BinanceFuturesAccountResponseDto response = binanceFuturesApiClient.getFuturesAccount();
//
//        assertNotNull(response);
//        System.out.println("Account 조회 응답: " + response);
//    }
//
//    @DisplayName("Binance Futures API : Price 조회")
//    @Test
//    void getPrice() throws Exception {
//        BinancePriceResponseDto response = binanceFuturesApiClient.getPrice("BTCUSDT");
//
//        assertNotNull(response);
//        System.out.println("Account 조회 응답: " + response);
//    }
//
//    @DisplayName("Binance Futures API : Price 전체 조회")
//    @Test
//    void getAllPrice() throws Exception {
//        List<BinancePriceResponseDto> response = binanceFuturesApiClient.getTotalPrice();
//
//        assertNotNull(response);
//        System.out.println("Account 조회 응답: " + response);
//    }
}
