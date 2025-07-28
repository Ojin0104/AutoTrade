package yj.AutoTrade.trade;

import org.junit.jupiter.api.Test;
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
import yj.AutoTrade.exception.ErrorCode;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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


    @Test
    void upbitAndBinanceSuccess() throws Exception {

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

        BinanceFuturesOrderResponseDto binanceOrderResponse = mock(BinanceFuturesOrderResponseDto.class);
        when(binanceOrderResponse.getOrderId()).thenReturn(123456L);

        // 잔고 및 가격 조회 API 모킹
        when(upbitApiClient.getUpbitAccount()).thenReturn(new UpbitAccountResponseDto[]{
            createMockAccount("KRW", "2000000")
        });
        when(upbitApiClient.getUpbitTicker("KRW-BTC")).thenReturn(new UpbitTickerResponseDto[]{createMockTicker("71000000")});
        when(upbitApiClient.getUpbitTicker("KRW-USDT")).thenReturn(new UpbitTickerResponseDto[]{createMockTicker("1350")});
        when(binanceFuturesApiClient.getMarkPrice("BTCUSDT")).thenReturn(createMockMarkPrice(new BigDecimal("52500")));
        when(binanceFuturesApiClient.getAccount()).thenReturn(createMockBinanceAccount());
        
        // 주문 실행 API 모킹
        when(upbitApiClient.createOrder(any(UpbitOrderRequestDto.class))).thenReturn(upbitOrderResponse);
        doNothing().when(binanceFuturesApiClient).changeLeverage(any(BinanceChangeLeverageRequestDto.class));
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class))).thenReturn(binanceOrderResponse);

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveSuccessfulOrder(request, upbitOrderResponse, binanceOrderResponse);
        verify(orderService, never()).saveFailedOrder(any(), any());
        verify(tradeCompensationQueueService, never()).saveToQueue(any(), any());
    }

    private UpbitAccountResponseDto createMockAccount(String currency, String balance) {
        UpbitAccountResponseDto account = mock(UpbitAccountResponseDto.class);
        when(account.getCurrency()).thenReturn(currency);
        when(account.getBalance()).thenReturn(balance);
        return account;
    }
    
    private UpbitTickerResponseDto createMockTicker(String price) {
        UpbitTickerResponseDto ticker = mock(UpbitTickerResponseDto.class);
        when(ticker.getTradePrice()).thenReturn(Double.parseDouble(price));
        return ticker;
    }
    
    private BinancePriceResponseDto createMockMarkPrice(BigDecimal price) {
        return new BinancePriceResponseDto("BTCUSDT", price, null, null, null, null, null, null);
    }
    
    private BinanceFuturesAccountResponseDto createMockBinanceAccount() {
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
            java.util.Arrays.asList(usdtAsset), java.util.Arrays.asList()
        );
    }

    @Test
    void upbitFail() {
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
    void binanceFailAndCompensationSuccess() throws Exception {
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
        when(upbitApiClient.getUpbitAccount()).thenReturn(new UpbitAccountResponseDto[]{createMockAccount("KRW", "2000000")});
        when(upbitApiClient.getUpbitTicker("KRW-BTC")).thenReturn(new UpbitTickerResponseDto[]{createMockTicker("71000000")});
        when(upbitApiClient.getUpbitTicker("KRW-USDT")).thenReturn(new UpbitTickerResponseDto[]{createMockTicker("1350")});
        when(binanceFuturesApiClient.getMarkPrice("BTCUSDT")).thenReturn(createMockMarkPrice(new BigDecimal("52500")));
        when(binanceFuturesApiClient.getAccount()).thenReturn(createMockBinanceAccount());
        
        // 업비트 주문은 성공, 바이낸스 주문은 실패
        when(upbitApiClient.createOrder(argThat(req -> "bid".equals(req.getSide())))).thenReturn(upbitOrderResponse);
        doNothing().when(binanceFuturesApiClient).changeLeverage(any(BinanceChangeLeverageRequestDto.class));
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class)))
                .thenThrow(new BinanceException(ErrorCode.BINANCE_API_ERROR));
        
        // 보상 매도 주문 성공
        when(upbitApiClient.createOrder(argThat(req -> "ask".equals(req.getSide())))).thenReturn(mock(UpbitOrderResponseDto.class));

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveFailedOrder(request, upbitOrderResponse);
        verify(tradeCompensationQueueService, never()).saveToQueue(any(), any());
    }

    @Test
    void binanceFailAndCompensationFail() throws Exception {
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
        //when(upbitOrderResponse.getPrice()).thenReturn(new BigDecimal("1000000"));
        when(upbitOrderResponse.getMarket()).thenReturn("KRW-BTC");

        // 성공적인 검증 및 가격 조회 설정
        when(upbitApiClient.getUpbitAccount()).thenReturn(new UpbitAccountResponseDto[]{createMockAccount("KRW", "2000000")});
        when(upbitApiClient.getUpbitTicker("KRW-BTC")).thenReturn(new UpbitTickerResponseDto[]{createMockTicker("71000000")});
        when(upbitApiClient.getUpbitTicker("KRW-USDT")).thenReturn(new UpbitTickerResponseDto[]{createMockTicker("1350")});
        when(binanceFuturesApiClient.getMarkPrice("BTCUSDT")).thenReturn(createMockMarkPrice(new BigDecimal("52500")));
        when(binanceFuturesApiClient.getAccount()).thenReturn(createMockBinanceAccount());
        
        // 업비트 주문은 성공, 바이낸스 주문은 실패
        when(upbitApiClient.createOrder(argThat(req -> "bid".equals(req.getSide())))).thenReturn(upbitOrderResponse);
        doNothing().when(binanceFuturesApiClient).changeLeverage(any(BinanceChangeLeverageRequestDto.class));
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class)))
                .thenThrow(new BinanceException(ErrorCode.BINANCE_API_ERROR));
        
        // 보상 매도 주문 실패
        when(upbitApiClient.createOrder(argThat(req -> "ask".equals(req.getSide()))))
                .thenThrow(new UpbitException(ErrorCode.UPBIT_API_ERROR));

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveFailedOrder(request, upbitOrderResponse);
        verify(tradeCompensationQueueService).saveToQueue(eq(upbitOrderResponse), any(UpbitException.class));
    }
}