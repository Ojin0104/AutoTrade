package yj.AutoTrade.trade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.*;
import yj.AutoTrade.api.binance.BinanceException;
import yj.AutoTrade.api.binance.BinanceFuturesApiClient;
import yj.AutoTrade.api.binance.dto.BinanceChangeLeverageRequestDto;
import yj.AutoTrade.api.binance.dto.BinanceFuturesOrderRequestDto;
import yj.AutoTrade.api.binance.dto.BinanceFuturesOrderResponseDto;
import yj.AutoTrade.trade.dto.TradeRequestDto;
import yj.AutoTrade.api.upbit.UpbitApiClient;
import yj.AutoTrade.api.upbit.UpbitException;
import yj.AutoTrade.api.upbit.dto.*;
import yj.AutoTrade.api.binance.dto.*;
import yj.AutoTrade.api.exchange.ExchangeRateApiClient;
import yj.AutoTrade.api.exchange.dto.KoreaEximExchangeRateDto;
import yj.AutoTrade.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @InjectMocks
    private TradeService tradeService;

    @Mock
    private UpbitApiClient upbitApiClient;
    @Mock
    private BinanceFuturesApiClient binanceFuturesApiClient;
    @Mock
    private OrderService orderService;
    @Mock
    private TradeCompensationQueueService tradeCompensationQueueService;
    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;
    @Mock
    private Executor tradeExecutor;


    @Test
    @DisplayName("업비트 현물 매수와 바이낸스 선물 매도가 모두 성공하는 경우")
    void upbitAndBinanceSuccess() throws Exception {
        // Mock Executor 설정 - 즉시 실행
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(tradeExecutor).execute(any(Runnable.class));

        // given
        TradeRequestDto request = TradeRequestDto.builder()
                .upbitSymbol("KRW-BTC")
                .binanceSymbol("BTCUSDT")
                .price(new BigDecimal("1000000"))
                .leverage(new BigDecimal("3"))
                .build();

        UpbitOrderResponseDto upbitOrderResponse = mock(UpbitOrderResponseDto.class);
        when(upbitOrderResponse.getUuid()).thenReturn("upbit-uuid");

        BinanceFuturesOrderResponseDto binanceOrderResponse = mock(BinanceFuturesOrderResponseDto.class);
        when(binanceOrderResponse.getOrderId()).thenReturn(123456L);

        // 잔고 및 가격 조회 API 모킹
        when(upbitApiClient.getUpbitAccount()).thenReturn(new UpbitAccountResponseDto[]{
            createAccount("KRW", "2000000")
        });
        when(binanceFuturesApiClient.getMarkPrice("BTCUSDT")).thenReturn(createMarkPrice(new BigDecimal("52500")));
        when(binanceFuturesApiClient.getAccount()).thenReturn(createBinanceAccount());
        when(exchangeRateApiClient.getUsdKrwExchangeRate()).thenReturn(createExchangeRates());
        
        // 주문 실행 API 모킹
        when(upbitApiClient.createOrder(any(UpbitOrderRequestDto.class))).thenReturn(upbitOrderResponse);
        when(binanceFuturesApiClient.changeLeverage(any(BinanceChangeLeverageRequestDto.class)))
                .thenReturn(mock(BinanceChangeLeverageResponseDto.class));
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class))).thenReturn(binanceOrderResponse);

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveSuccessfulOrder(request, upbitOrderResponse, binanceOrderResponse);
        verify(orderService, never()).saveFailedOrder(any(), any());
        verify(tradeCompensationQueueService, never()).saveToQueue(any(), any());
    }

    private UpbitAccountResponseDto createAccount(String currency, String balance) {
        return new UpbitAccountResponseDto(
            currency,
            balance,
            "0",        // locked
            "0",        // avgBuyPrice
            false,      // avgBuyPriceModified
            "KRW"       // unitCurrency
        );
    }
    
    private UpbitTickerResponseDto createTicker(String price) {
        return new UpbitTickerResponseDto(
            "KRW-BTC",                    // market
            "20240101",                   // tradeDate
            "120000",                     // tradeTime
            "20240101",                   // tradeDateKst
            "210000",                     // tradeTimeKst
            System.currentTimeMillis(),   // tradeTimestamp
            Double.parseDouble(price),    // openingPrice
            Double.parseDouble(price),    // highPrice
            Double.parseDouble(price),    // lowPrice
            Double.parseDouble(price),    // tradePrice
            Double.parseDouble(price),    // prevClosingPrice
            "EVEN",                       // change
            0.0,                          // changePrice
            0.0,                          // changeRate
            0.0,                          // signedChangePrice
            0.0,                          // signedChangeRate
            0.01,                         // tradeVolume
            1000000.0,                    // accTradePrice
            1000000.0,                    // accTradePrice24h
            0.01,                         // accTradeVolume
            0.01,                         // accTradeVolume24h
            Double.parseDouble(price),    // highest52WeekPrice
            "2024-01-01",                // highest52WeekDate
            Double.parseDouble(price),    // lowest52WeekPrice
            "2024-01-01",                // lowest52WeekDate
            System.currentTimeMillis()    // timestamp
        );
    }
    
    private BinancePriceResponseDto createMarkPrice(BigDecimal price) {
        return new BinancePriceResponseDto("BTCUSDT", price, null, null, null, null, null, null);
    }
    
    private BinanceFuturesAccountResponseDto createBinanceAccount() {
        BinanceFuturesAccountResponseDto.Asset usdtAsset = 
            new BinanceFuturesAccountResponseDto.Asset(
                "USDT", new BigDecimal("1000"), new BigDecimal("0"), new BigDecimal("1000"), 
                new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), 
                new BigDecimal("1000"), new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("1000"), 
                System.currentTimeMillis(), true
            );
        return new BinanceFuturesAccountResponseDto(
            new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("0"),
            new BigDecimal("1000"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("1000"),
            new BigDecimal("0"), new BigDecimal("1000"), new BigDecimal("1000"),
            List.of(usdtAsset), List.of()
        );
    }
    
    private List<KoreaEximExchangeRateDto> createExchangeRates() {
        KoreaEximExchangeRateDto rate = new KoreaEximExchangeRateDto();
        ReflectionTestUtils.setField(rate, "dealBasR", "1,350.00");
        return List.of(rate);
    }

    @Test
    @DisplayName("업비트 계좌 확인 실패로 거래가 중단되는 경우")
    void upbitAccountCheckFailAndTradeAborted() {
        // given
        TradeRequestDto request = TradeRequestDto.builder()
                .upbitSymbol("KRW-BTC")
                .binanceSymbol("BTCUSDT")
                .price(new BigDecimal("1000000"))
                .leverage(new BigDecimal("3"))
                .build();

        // 잔고 확인에서 실패하도록 설정
        when(upbitApiClient.getUpbitAccount())
                .thenThrow(new UpbitException(ErrorCode.UPBIT_API_ERROR));

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveFailedOrder(eq(request), isNull());
        verify(orderService, never()).saveSuccessfulOrder(
                any(TradeRequestDto.class),
                any(UpbitOrderResponseDto.class),
                any(BinanceFuturesOrderResponseDto.class)
        );
        verify(tradeCompensationQueueService, never()).saveToQueue(any(), any());
    }

    @Test
    @DisplayName("바이낸스 주문 실패 후 업비트 보상 매도가 성공하는 경우")
    void binanceFailAndCompensationSuccess() throws Exception {
        // Mock Executor 설정 - 즉시 실행
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(tradeExecutor).execute(any(Runnable.class));

        // given
        TradeRequestDto request = TradeRequestDto.builder()
                .upbitSymbol("KRW-BTC")
                .binanceSymbol("BTCUSDT")
                .price(new BigDecimal("1000000"))
                .leverage(new BigDecimal("3"))
                .build();

        UpbitOrderResponseDto upbitOrderResponse = mock(UpbitOrderResponseDto.class);
        when(upbitOrderResponse.getUuid()).thenReturn("upbit-uuid");
        when(upbitOrderResponse.getVolume()).thenReturn(new BigDecimal("0.01"));
//        when(upbitOrderResponse.getPrice()).thenReturn(new BigDecimal("1000000"));
        when(upbitOrderResponse.getMarket()).thenReturn("KRW-BTC");

        // 성공적인 검증 및 가격 조회 설정
        when(upbitApiClient.getUpbitAccount()).thenReturn(new UpbitAccountResponseDto[]{createAccount("KRW", "2000000")});
        when(binanceFuturesApiClient.getMarkPrice("BTCUSDT")).thenReturn(createMarkPrice(new BigDecimal("52500")));
        when(binanceFuturesApiClient.getAccount()).thenReturn(createBinanceAccount());
        when(exchangeRateApiClient.getUsdKrwExchangeRate()).thenReturn(createExchangeRates());
        
        // 업비트 주문은 성공, 바이낸스 주문은 실패
        when(upbitApiClient.createOrder(argThat(req -> req != null && "bid".equals(req.getSide())))).thenReturn(upbitOrderResponse);
        when(binanceFuturesApiClient.changeLeverage(any(BinanceChangeLeverageRequestDto.class)))
                .thenReturn(mock(BinanceChangeLeverageResponseDto.class));
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class)))
                .thenThrow(new BinanceException(ErrorCode.BINANCE_API_ERROR));
        
        // 보상 매도 주문 성공
        when(upbitApiClient.createOrder(argThat(req -> req != null && "ask".equals(req.getSide())))).thenReturn(mock(UpbitOrderResponseDto.class));

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveFailedOrder(request, upbitOrderResponse);
        verify(tradeCompensationQueueService, never()).saveToQueue(any(), any());
    }

    @Test
    @DisplayName("바이낸스 주문 실패 후 업비트 보상 매도도 실패하여 큐에 저장되는 경우")
    void binanceFailAndCompensationFail() throws Exception {
        // Mock Executor 설정 - 즉시 실행
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(tradeExecutor).execute(any(Runnable.class));

        // given
        TradeRequestDto request = TradeRequestDto.builder()
                .upbitSymbol("KRW-BTC")
                .binanceSymbol("BTCUSDT")
                .price(new BigDecimal("1000000"))
                .leverage(new BigDecimal("3"))
                .build();

        UpbitOrderResponseDto upbitOrderResponse = mock(UpbitOrderResponseDto.class);
        when(upbitOrderResponse.getVolume()).thenReturn(new BigDecimal("0.01"));
        when(upbitOrderResponse.getMarket()).thenReturn("KRW-BTC");

        // 성공적인 검증 및 가격 조회 설정
        when(upbitApiClient.getUpbitAccount()).thenReturn(new UpbitAccountResponseDto[]{createAccount("KRW", "2000000")});
        when(binanceFuturesApiClient.getMarkPrice("BTCUSDT")).thenReturn(createMarkPrice(new BigDecimal("52500")));
        when(binanceFuturesApiClient.getAccount()).thenReturn(createBinanceAccount());
        when(exchangeRateApiClient.getUsdKrwExchangeRate()).thenReturn(createExchangeRates());
        
        // 업비트 주문은 성공, 바이낸스 주문은 실패
        when(upbitApiClient.createOrder(argThat(req -> req != null && "bid".equals(req.getSide())))).thenReturn(upbitOrderResponse);
        when(binanceFuturesApiClient.changeLeverage(any(BinanceChangeLeverageRequestDto.class)))
                .thenReturn(mock(BinanceChangeLeverageResponseDto.class));
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class)))
                .thenThrow(new BinanceException(ErrorCode.BINANCE_API_ERROR));
        
        // 보상 매도 주문 실패
        when(upbitApiClient.createOrder(argThat(req -> req != null && "ask".equals(req.getSide()))))
                .thenThrow(new UpbitException(ErrorCode.UPBIT_API_ERROR));

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveFailedOrder(request, upbitOrderResponse);
        verify(tradeCompensationQueueService).saveToQueue(eq(upbitOrderResponse), any(UpbitException.class));
    }
}