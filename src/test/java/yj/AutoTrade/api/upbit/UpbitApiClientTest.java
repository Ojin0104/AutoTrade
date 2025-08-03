package yj.AutoTrade.api.upbit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yj.AutoTrade.api.upbit.UpbitApiClient;
import yj.AutoTrade.api.upbit.dto.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
    }

    @DisplayName("Upbit API : 사용자 계좌 조회")
    @Test
    void testGetUpbitAccount() {

        UpbitAccountResponseDto[] upbitAccount = upbitApiClient.getUpbitAccount();

        assertNotNull(upbitAccount);
    }


    @DisplayName("Upbit API : 주문 (정상 응답)")
    @Test
    void testCreateUpbitOrder() throws UnsupportedEncodingException, NoSuchAlgorithmException {

        UpbitOrderRequestDto upbitOrderRequestDto = UpbitOrderRequestDto.builder()
                .market("KRW-BTC")
                .side("bid")
                .price(new BigDecimal("5000"))
                .ordType(UpbitOrderType.PRICE)
                .build();
        UpbitOrderResponseDto orderResponse = upbitApiClient.createOrder(upbitOrderRequestDto);

        assertNotNull(orderResponse);
    }
}