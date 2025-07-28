package yj.AutoTrade.trade;

import org.junit.jupiter.api.Test;
import org.mockito.*;
import yj.AutoTrade.api.binance.BinanceException;
import yj.AutoTrade.api.binance.BinanceFuturesApiClient;
import yj.AutoTrade.api.binance.dto.BinanceChangeLeverageRequestDto;
import yj.AutoTrade.api.binance.dto.BinanceFuturesOrderRequestDto;
import yj.AutoTrade.api.binance.dto.BinanceFuturesOrderResponseDto;
import yj.AutoTrade.api.binance.dto.BinanceOrderResponseDto;
import yj.AutoTrade.trade.dto.TradeRequestDto;
import yj.AutoTrade.api.upbit.UpbitApiClient;
import yj.AutoTrade.api.upbit.UpbitException;
import yj.AutoTrade.api.upbit.dto.UpbitOrderRequestDto;
import yj.AutoTrade.api.upbit.dto.UpbitOrderResponseDto;

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

        when(upbitApiClient.createOrder(any(UpbitOrderRequestDto.class))).thenReturn(upbitOrderResponse);
        when(binanceFuturesApiClient.changeLeverage(any(BinanceChangeLeverageRequestDto.class))).thenReturn(null);
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class))).thenReturn(binanceOrderResponse);

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveSuccessfulOrder(request, upbitOrderResponse, binanceOrderResponse);
        verify(orderService, never()).saveFailedOrder(any(), any());
        verify(tradeCompensationQueueService, never()).saveToQueue(any(), any());
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

        when(upbitApiClient.createOrder(any(UpbitOrderRequestDto.class)))
                .thenThrow(new UpbitException("error", "upbit fail"));

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveFailedOrder(eq(request), isNull());
        verify(orderService, never()).saveSuccessfulOrder(
                any(TradeRequestDto.class),
                any(UpbitOrderResponseDto.class),
                any(BinanceOrderResponseDto.class)
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

        when(upbitApiClient.createOrder(any(UpbitOrderRequestDto.class))).thenReturn(upbitOrderResponse);
//        doNothing().when(binanceFuturesApiClient).changeLeverage(any(BinanceChangeLeverageRequestDto.class));
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class)))
                .thenThrow(new BinanceException("error", "binance fail"));
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

        when(upbitApiClient.createOrder(any(UpbitOrderRequestDto.class))).thenReturn(upbitOrderResponse);
        //doNothing().when(binanceFuturesApiClient).changeLeverage(any(BinanceChangeLeverageRequestDto.class));
        when(binanceFuturesApiClient.createOrder(any(BinanceFuturesOrderRequestDto.class)))
                .thenThrow(new BinanceException("error", "binance fail"));
        when(upbitApiClient.createOrder(argThat(req -> "ask".equals(req.getSide()))))
                .thenThrow(new UpbitException("error", "compensation fail"));

        // when
        tradeService.trade(request);

        // then
        verify(orderService).saveFailedOrder(request, upbitOrderResponse);
        verify(tradeCompensationQueueService).saveToQueue(eq(upbitOrderResponse), any(UpbitException.class));
    }
}