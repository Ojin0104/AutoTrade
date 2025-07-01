package yj.AutoTrade.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yj.AutoTrade.binance.BinanceException;
import yj.AutoTrade.binance.BinanceFuturesApiClient;
import yj.AutoTrade.binance.dto.BinanceFuturesOrderRequestDto;
import yj.AutoTrade.binance.dto.BinanceFuturesOrderResponseDto;
import yj.AutoTrade.binance.dto.BinanceChangeLeverageRequestDto;
import yj.AutoTrade.binance.dto.OrderSide;
import yj.AutoTrade.binance.dto.OrderType;
import yj.AutoTrade.binance.dto.NewOrderRespType;
import yj.AutoTrade.trade.dto.TradeRequestDto;
import yj.AutoTrade.upbit.UpbitApiClient;
import yj.AutoTrade.upbit.UpbitException;
import yj.AutoTrade.upbit.dto.UpbitOrderRequestDto;
import yj.AutoTrade.upbit.dto.UpbitOrderResponseDto;
import yj.AutoTrade.upbit.dto.UpbitOrderType;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final UpbitApiClient upbitApiClient;
    private final BinanceFuturesApiClient binanceFuturesApiClient;
    private final OrderService orderService;
    private final TradeCompensationQueueService tradeCompensationQueueService;

    public void trade(TradeRequestDto tradeRequestDto) {
        UpbitOrderResponseDto upbitOrderResponse = null;
        try {
            // 1. Upbit 시장가 매수
            UpbitOrderRequestDto upbitOrderRequest = UpbitOrderRequestDto.builder()
                    .market(tradeRequestDto.getUpbitSymbol())
                    .price(tradeRequestDto.getPrice())
                    .ordType(UpbitOrderType.MARKET)
                    .side("bid")
                    .build();
            upbitOrderResponse = upbitApiClient.createOrder(upbitOrderRequest);
            log.info("Upbit 매수 주문 성공: {}", upbitOrderResponse.getUuid());

            // 2. Binance 선물 레버리지 설정
            binanceFuturesApiClient.changeLeverage(
                BinanceChangeLeverageRequestDto.builder()
                    .symbol(tradeRequestDto.getBinanceSymbol())
                    .leverage(tradeRequestDto.getLeverage().intValue())
                    .build()
            );

            // 3. 배율에 따라 수량 조절 (업비트 체결 수량 * 레버리지)
            BigDecimal upbitVolume = upbitOrderResponse.getVolume();
            BigDecimal leverage = tradeRequestDto.getLeverage();
            BigDecimal futuresQuantity = upbitVolume.multiply(leverage);

            // 바이낸스 최소 주문 단위(예: 0.001 등) 반올림 필요시 아래 코드 사용
            // futuresQuantity = futuresQuantity.setScale(3, RoundingMode.DOWN);

            // 4. Binance 선물 숏 포지션 주문
            BinanceFuturesOrderRequestDto binanceFuturesOrderRequest = BinanceFuturesOrderRequestDto.builder()
                    .symbol(tradeRequestDto.getBinanceSymbol())
                    .side(OrderSide.SELL)
                    .type(OrderType.MARKET)
                    .quantity(futuresQuantity.toPlainString())
                    .newClientOrderId("short-" + System.currentTimeMillis())
                    .newOrderRespType(NewOrderRespType.FULL)
                    .recvWindow(5000L)
                    .timestamp(System.currentTimeMillis())
                    .build();
            BinanceFuturesOrderResponseDto binanceOrderResponse = binanceFuturesApiClient.createOrder(binanceFuturesOrderRequest);
            log.info("Binance 선물 숏 주문 성공: {}", binanceOrderResponse.getOrderId());

            // 5. 주문 저장
            orderService.saveSuccessfulOrder(tradeRequestDto, upbitOrderResponse, binanceOrderResponse);
            log.info("신규 포지션 저장 성공");

        } catch (UpbitException e) {
            log.error("Upbit 주문 실패: {}", e.getMessage());
            orderService.saveFailedOrder(tradeRequestDto, null);

        } catch (BinanceException e) {
            log.error("Binance 주문 실패: {}", e.getMessage());
            if (upbitOrderResponse != null) {
                orderService.saveFailedOrder(tradeRequestDto, upbitOrderResponse);
                attemptCompensation(upbitOrderResponse);
            }
        } catch (Exception e) {
            log.error("기타 예외 발생: {}", e.getMessage());
            if (upbitOrderResponse != null) {
                orderService.saveFailedOrder(tradeRequestDto, upbitOrderResponse);
                attemptCompensation(upbitOrderResponse);
            }
        }
    }

    private void attemptCompensation(UpbitOrderResponseDto upbitOrderResponse) {
        try {
            UpbitOrderRequestDto compensateRequest = UpbitOrderRequestDto.builder()
                    .market(upbitOrderResponse.getMarket())
                    .volume(upbitOrderResponse.getVolume())
                    .ordType(UpbitOrderType.MARKET)
                    .side("ask") // 매도
                    .build();
            upbitApiClient.createOrder(compensateRequest);
            log.info("즉시 보상 매도 주문 성공. Upbit Order: {}", upbitOrderResponse.getUuid());
        } catch (UpbitException compensationException) {
            log.error("즉시 보상 매도 주문 실패: {}", compensationException.getMessage());
            tradeCompensationQueueService.saveToQueue(upbitOrderResponse, compensationException);
        }
    }
}
