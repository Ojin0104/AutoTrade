package yj.AutoTrade.upbit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yj.AutoTrade.upbit.dto.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UpbitApiClientTest {
    @Autowired
    private UpbitApiClient upbitApiClient;

    @DisplayName("Upbit API : 실시간 거래가 조회")
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

    @DisplayName("Upbit API : 사용자 계좌 조회")
    @Test
    void testGetUpbitAccount() {

        UpbitAccountResponseDto[] upbitAccount = upbitApiClient.getUpbitAccount();

        assertNotNull(upbitAccount);

        System.out.println("Upbit API Response: " + upbitAccount[0]);
    }


    @DisplayName("Upbit API : 주문 (정상 응답)")
    @Test
    void testCreateUpbitOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        UpbitOrderRequestDto upbitOrderRequestDto = UpbitOrderRequestDto.builder()
                .market("KRW-BTC")
                .side("bid")
                .price("5000")
                .ordType("price")
                .build();
        UpbitOrderResponseDto orderResponse = upbitApiClient.createOrder(upbitOrderRequestDto);

        assertNotNull(orderResponse);
        System.out.println("Upbit API Response: " + orderResponse);
    }
}