package yj.AutoTrade.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import yj.AutoTrade.api.binance.BinanceApiClient;
import yj.AutoTrade.api.binance.dto.BinanceTickerPriceDto;
import yj.AutoTrade.trade.PriceGapCalculator;
import yj.AutoTrade.api.upbit.UpbitApiClient;
import yj.AutoTrade.api.upbit.dto.UpbitTickerResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceGapCalculatorServiceTest {

    @Mock
    private BinanceApiClient binanceApiClient;

    @Mock
    private UpbitApiClient upbitApiClient;

    @InjectMocks
    private PriceGapCalculator priceGapCalculator;

    @Test
    void calculatePremiums_ShouldCalculateCorrectly() throws Exception {
        // Given
        String upbitMarket = "KRW-BTC";
        String binanceSymbol = "BTCUSDT";

        // Upbit mock response
        UpbitTickerResponseDto upbitResponse = new UpbitTickerResponseDto();
        upbitResponse.setMarket(upbitMarket);
        upbitResponse.setTradePrice(50000000.0); // 5천만원
        when(upbitApiClient.getUpbitTicker(upbitMarket)).thenReturn(new UpbitTickerResponseDto[]{upbitResponse});

        // Binance mock response
        BinanceTickerPriceDto binanceResponse = new BinanceTickerPriceDto();
        binanceResponse.setSymbol(binanceSymbol);
        binanceResponse.setPrice(48000000.0); // 4천8백만원
        when(binanceApiClient.getTickerPrice(binanceSymbol)).thenReturn(binanceResponse);

        // When
        BigDecimal result = priceGapCalculator.calculatePremium(upbitMarket,binanceSymbol);

        // Then
        assertNotNull(result);

        // 김치프리미엄 계산: ((업비트가격 - 바이낸스가격) / 바이낸스가격) * 100
        double expectedPremium = ((50000000.0 - 48000000.0) / 48000000.0) * 100;
        BigDecimal expected = BigDecimal.valueOf(expectedPremium).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, result);
    }

   
} 