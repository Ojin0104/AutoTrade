package yj.AutoTrade.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import yj.AutoTrade.api.binance.BinanceException;
import yj.AutoTrade.api.binance.BinanceFuturesApiClient;
import yj.AutoTrade.api.binance.dto.BinanceFuturesOrderRequestDto;
import yj.AutoTrade.api.binance.dto.BinanceFuturesOrderResponseDto;
import yj.AutoTrade.api.binance.dto.BinanceChangeLeverageRequestDto;
import yj.AutoTrade.api.binance.dto.OrderSide;
import yj.AutoTrade.api.binance.dto.OrderType;
import yj.AutoTrade.api.binance.dto.NewOrderRespType;
import yj.AutoTrade.api.exchange.ExchangeRateApiClient;
import yj.AutoTrade.api.exchange.dto.KoreaEximExchangeRateDto;
import yj.AutoTrade.api.upbit.UpbitApiClient;
import yj.AutoTrade.api.upbit.UpbitException;
import yj.AutoTrade.api.upbit.dto.UpbitAccountResponseDto;
import yj.AutoTrade.api.upbit.dto.UpbitOrderRequestDto;
import yj.AutoTrade.api.upbit.dto.UpbitOrderResponseDto;
import yj.AutoTrade.api.upbit.dto.UpbitOrderType;
import yj.AutoTrade.api.upbit.dto.UpbitTickerResponseDto;
import yj.AutoTrade.api.binance.dto.BinancePriceResponseDto;
import yj.AutoTrade.api.binance.dto.BinanceFuturesAccountResponseDto;
import yj.AutoTrade.exception.ErrorCode;
import yj.AutoTrade.trade.dto.TradeRequestDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final UpbitApiClient upbitApiClient;
    private final BinanceFuturesApiClient binanceFuturesApiClient;
    private final OrderService orderService;
    private final TradeCompensationQueueService tradeCompensationQueueService;
    private final ExchangeRateApiClient exchangeRateApiClient;
    
    @Qualifier("tradeExecutor")
    private final Executor tradeExecutor;

    public void trade(TradeRequestDto tradeRequestDto) {
        try {
            // 1. 잔고 및 가격 확인 (실패시 예외 발생)
            validateBalanceAndPrice(tradeRequestDto);

            // 2. 현재 가격 조회로 수량 계산
            BigDecimal binancePrice = getCurrentBinancePrice(tradeRequestDto.getBinanceSymbol());
            
            // 3. 금액 기준 수량 계산
            BigDecimal tradeAmount = tradeRequestDto.getPrice(); // KRW 금액
            BigDecimal leverage = tradeRequestDto.getLeverage();

            
            // 바이낸스 주문 수량 (KRW를 USD로 환산 후 레버리지 적용)
            BigDecimal usdKrwRate = getCurrentUsdKrwRate(); // 실제 USD/KRW 환율 조회
            BigDecimal usdAmount = tradeAmount.divide(usdKrwRate, 2, java.math.RoundingMode.DOWN);
            BigDecimal binanceQuantity = usdAmount.multiply(leverage).divide(binancePrice, 8, java.math.RoundingMode.DOWN);

            // 4. 레버리지 설정
            binanceFuturesApiClient.changeLeverage(
                BinanceChangeLeverageRequestDto.builder()
                    .symbol(tradeRequestDto.getBinanceSymbol())
                    .leverage(leverage.intValue())
                    .build()
            );

            // 5. 동시 주문 실행
            UpbitOrderRequestDto upbitOrderRequest = UpbitOrderRequestDto.builder()
                    .market(tradeRequestDto.getUpbitSymbol())
                    .price(tradeAmount)
                    .ordType(UpbitOrderType.MARKET)
                    .side("bid")
                    .build();

            BinanceFuturesOrderRequestDto binanceOrderRequest = BinanceFuturesOrderRequestDto.builder()
                    .symbol(tradeRequestDto.getBinanceSymbol())
                    .side(OrderSide.SELL)
                    .type(OrderType.MARKET)
                    .quantity(binanceQuantity.toPlainString())
                    .newClientOrderId("short-" + System.currentTimeMillis())
                    .newOrderRespType(NewOrderRespType.FULL)
                    .recvWindow(5000L)
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 6. 진짜 동시 주문 실행
            CompletableFuture<UpbitOrderResponseDto> upbitFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return upbitApiClient.createOrder(upbitOrderRequest);
                } catch (UpbitException e) {
                    log.error("Upbit 주문 실패: {}", e.getMessage());
                    return null;
                } catch (Exception e) {
                    log.error("Upbit 주문 중 예외 발생: {}", e.getMessage());
                    return null;
                }
            }, tradeExecutor);

            CompletableFuture<BinanceFuturesOrderResponseDto> binanceFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return binanceFuturesApiClient.createOrder(binanceOrderRequest);
                } catch (BinanceException e) {
                    log.error("Binance 주문 실패: {}", e.getMessage());
                    return null;
                } catch (Exception e) {
                    log.error("Binance 주문 중 예외 발생: {}", e.getMessage());
                    return null;
                }
            }, tradeExecutor);

            // 7. 주문 결과 대기
            UpbitOrderResponseDto upbitOrderResponse = upbitFuture.get();
            BinanceFuturesOrderResponseDto binanceOrderResponse = binanceFuture.get();

            // 8. 결과 처리
            handleTradeResult(tradeRequestDto, upbitOrderResponse, binanceOrderResponse);

        } catch (InterruptedException e) {
            log.error("주문 실행 중 인터럽트 발생: {}", e.getMessage());
            Thread.currentThread().interrupt();
            saveFailedOrderResult(tradeRequestDto, null);
        } catch (BinanceException e) {
            log.error("레버리지 설정 실패: {}", e.getMessage());
            saveFailedOrderResult(tradeRequestDto, null);
        } catch (Exception e) {
            log.error("기타 예외 발생: {}", e.getMessage());
            saveFailedOrderResult(tradeRequestDto, null);
        }
    }

    public void closeTrade(TradeRequestDto tradeRequestDto) {
        try {
            // 1. 현재 가격 조회로 수량 계산
            BigDecimal upbitPrice = getCurrentUpbitPrice(tradeRequestDto.getUpbitSymbol());
            BigDecimal binancePrice = getCurrentBinancePrice(tradeRequestDto.getBinanceSymbol());

            // 2. 기존 포지션에 해당하는 수량으로 역방향 거래
            BigDecimal tradeAmount = tradeRequestDto.getPrice();
            BigDecimal leverage = tradeRequestDto.getLeverage();

            // 업비트 매도 수량 (보유 수량)
            BigDecimal upbitQuantity = tradeAmount.divide(upbitPrice, 8, java.math.RoundingMode.DOWN);

            // 바이낸스 숏 포지션 정리 수량 (기존 숏과 동일한 수량으로 BUY)
            BigDecimal usdKrwRate = getCurrentUsdKrwRate();
            BigDecimal usdAmount = tradeAmount.divide(usdKrwRate, 2, java.math.RoundingMode.DOWN);
            BigDecimal binanceQuantity = usdAmount.multiply(leverage).divide(binancePrice, 8, java.math.RoundingMode.DOWN);

            // 3. 동시 주문 실행 (역방향)
            UpbitOrderRequestDto upbitOrderRequest = UpbitOrderRequestDto.builder()
                    .market(tradeRequestDto.getUpbitSymbol())
                    .volume(upbitQuantity)
                    .ordType(UpbitOrderType.MARKET)
                    .side("ask") // 매도
                    .build();

            BinanceFuturesOrderRequestDto binanceOrderRequest = BinanceFuturesOrderRequestDto.builder()
                    .symbol(tradeRequestDto.getBinanceSymbol())
                    .side(OrderSide.BUY) // 숏 정리
                    .type(OrderType.MARKET)
                    .quantity(binanceQuantity.toPlainString())
                    .newClientOrderId("close-" + System.currentTimeMillis())
                    .newOrderRespType(NewOrderRespType.FULL)
                    .recvWindow(5000L)
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 4. 동시 주문 실행
            CompletableFuture<UpbitOrderResponseDto> upbitFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return upbitApiClient.createOrder(upbitOrderRequest);
                } catch (UpbitException e) {
                    log.error("Upbit 매도 주문 실패: {}", e.getMessage());
                    return null;
                } catch (Exception e) {
                    log.error("Upbit 매도 주문 중 예외 발생: {}", e.getMessage());
                    return null;
                }
            }, tradeExecutor);

            CompletableFuture<BinanceFuturesOrderResponseDto> binanceFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return binanceFuturesApiClient.createOrder(binanceOrderRequest);
                } catch (BinanceException e) {
                    log.error("Binance 숏 정리 주문 실패: {}", e.getMessage());
                    return null;
                } catch (Exception e) {
                    log.error("Binance 숏 정리 주문 중 예외 발생: {}", e.getMessage());
                    return null;
                }
            }, tradeExecutor);

            // 5. 주문 결과 대기
            UpbitOrderResponseDto upbitOrderResponse = upbitFuture.get();
            BinanceFuturesOrderResponseDto binanceOrderResponse = binanceFuture.get();

            // 6. 결과 처리
            handleCloseTradeResult(tradeRequestDto, upbitOrderResponse, binanceOrderResponse);

        } catch (InterruptedException e) {
            log.error("포지션 정리 중 인터럽트 발생: {}", e.getMessage());
            Thread.currentThread().interrupt();
            saveFailedCloseOrderResult(tradeRequestDto, null);
        } catch (Exception e) {
            log.error("포지션 정리 중 기타 예외 발생: {}", e.getMessage());
            saveFailedCloseOrderResult(tradeRequestDto, null);
        }
    }

    private void saveOrderResult(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrderResponse, BinanceFuturesOrderResponseDto binanceOrderResponse) {
        try {
            orderService.saveSuccessfulOrder(tradeRequestDto, upbitOrderResponse, binanceOrderResponse);
            log.info("신규 포지션 저장 성공");
        } catch (Exception e) {
            log.error("주문 결과 저장 실패: {}", e.getMessage());
        }
    }

    private void saveFailedOrderResult(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrderResponse) {
        try {
            orderService.saveFailedOrder(tradeRequestDto, upbitOrderResponse);
        } catch (Exception e) {
            log.error("실패한 주문 저장 실패: {}", e.getMessage());
        }
    }


    private void validateBalanceAndPrice(TradeRequestDto tradeRequestDto) {
        try {
            // 1. 업비트 KRW 잔고 확인
            UpbitAccountResponseDto[] upbitAccounts = upbitApiClient.getUpbitAccount();
            BigDecimal krwBalance = BigDecimal.ZERO;
            for (UpbitAccountResponseDto account : upbitAccounts) {
                if ("KRW".equals(account.getCurrency())) {
                    krwBalance = new BigDecimal(account.getBalance());
                    break;
                }
            }
            
            if (krwBalance.compareTo(tradeRequestDto.getPrice()) < 0) {
                throw new UpbitException(ErrorCode.UPBIT_INSUFFICIENT_BALANCE);
            }
            
            // 2. 바이낸스 USDT 잔고 확인
            BigDecimal requiredUsdAmount = tradeRequestDto.getPrice().divide(getCurrentUsdKrwRate(), 2, java.math.RoundingMode.UP);
            validateBinanceBalance(requiredUsdAmount);
            
        } catch (UpbitException | BinanceException e) {
            throw e; // 재던지기
        } catch (Exception e) {
            log.error("잔고 확인 중 오류 발생: {}", e.getMessage());
            throw new UpbitException(ErrorCode.TRADE_BALANCE_VALIDATION_FAILED, e);
        }
    }

    private BigDecimal getCurrentUpbitPrice(String symbol) {
        try {
            UpbitTickerResponseDto[] ticker = upbitApiClient.getUpbitTicker(symbol);
            if (ticker != null && ticker.length > 0) {
                return new BigDecimal(ticker[0].getTradePrice());
            }
            throw new RuntimeException("업비트 가격 조회 실패");
        } catch (Exception e) {
            log.error("업비트 가격 조회 실패: {}", e.getMessage());
            throw new RuntimeException("업비트 가격 조회 실패", e);
        }
    }

    private BigDecimal getCurrentBinancePrice(String symbol) {
        try {
            BinancePriceResponseDto priceInfo = binanceFuturesApiClient.getMarkPrice(symbol);
            if (priceInfo != null && priceInfo.markPrice() != null) {
                return priceInfo.markPrice();
            }
            throw new RuntimeException("바이낸스 가격 조회 실패");
        } catch (Exception e) {
            log.error("바이낸스 가격 조회 실패: {}", e.getMessage());
            throw new RuntimeException("바이낸스 가격 조회 실패", e);
        }
    }

    private BigDecimal getCurrentUsdKrwRate() {
        try {
            // 한국수출입은행 실제 USD/KRW 환율 조회
            List<KoreaEximExchangeRateDto> exchangeRates = exchangeRateApiClient.getUsdKrwExchangeRate();
            if (exchangeRates != null && !exchangeRates.isEmpty()) {
                // 첫 번째 환율 정보의 매매기준율 사용
                return new BigDecimal(exchangeRates.get(0).getDealBasR().replace(",", ""));
            }
            throw new RuntimeException("USD-KRW 환율 조회 실패");
        } catch (Exception e) {
            log.error("USD-KRW 환율 조회 실패: {}", e.getMessage());
            throw new RuntimeException("USD-KRW 환율 조회 실패", e);
        }
    }

    private void validateBinanceBalance(BigDecimal requiredUsdAmount) {
        try {
            BinanceFuturesAccountResponseDto account = binanceFuturesApiClient.getAccount();
            if (account == null || account.assets() == null) {
                throw new BinanceException(ErrorCode.BINANCE_API_ERROR);
            }

            // USDT 자산 찾기
            for (BinanceFuturesAccountResponseDto.Asset asset : account.assets()) {
                if ("USDT".equals(asset.asset())) {
                    BigDecimal availableBalance = asset.availableBalance();
                    if (availableBalance.compareTo(requiredUsdAmount) >= 0) {
                        log.info("바이낸스 USDT 잔고 확인: 필요={}, 보유={}", requiredUsdAmount, availableBalance);
                        return;
                    } else {
                        throw new BinanceException(ErrorCode.BINANCE_INSUFFICIENT_BALANCE);
                    }
                }
            }
            
            throw new BinanceException(ErrorCode.BINANCE_API_ERROR);
        } catch (BinanceException e) {
            throw e;
        } catch (Exception e) {
            log.error("바이낸스 잔고 확인 중 오류 발생: {}", e.getMessage());
            throw new BinanceException(ErrorCode.TRADE_BALANCE_VALIDATION_FAILED, e);
        }
    }

    private void attemptBinancePositionClose(String symbol, BinanceFuturesOrderResponseDto binanceOrderResponse) {
        try {
            // 생성된 숏 포지션 수량만큼 BUY 주문으로 정리
            BigDecimal positionQuantity = new BigDecimal(binanceOrderResponse.getExecutedQty());
            
            BinanceFuturesOrderResponseDto closeOrderResponse = binanceFuturesApiClient.closePosition(symbol, positionQuantity);
            log.info("바이낸스 포지션 정리 성공: Symbol={}, Quantity={}, OrderId={}", 
                    symbol, positionQuantity, closeOrderResponse.getOrderId());
        } catch (BinanceException e) {
            log.error("바이낸스 포지션 정리 실패: Symbol={}, Error={}", symbol, e.getMessage());
            // TODO: 포지션 정리 실패시 알림 또는 재시도 로직 추가
        } catch (Exception e) {
            log.error("바이낸스 포지션 정리 중 예외 발생: Symbol={}, Error={}", symbol, e.getMessage());
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



    private void saveCloseOrderResult(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrderResponse, BinanceFuturesOrderResponseDto binanceOrderResponse) {
        try {
            orderService.saveCloseOrder(tradeRequestDto, upbitOrderResponse, binanceOrderResponse);
            log.info("포지션 정리 결과 저장 성공");
        } catch (Exception e) {
            log.error("포지션 정리 결과 저장 실패: {}", e.getMessage());
        }
    }

    private void saveFailedCloseOrderResult(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrderResponse) {
        try {
            orderService.saveFailedCloseOrder(tradeRequestDto, upbitOrderResponse);
        } catch (Exception e) {
            log.error("실패한 포지션 정리 주문 저장 실패: {}", e.getMessage());
        }
    }

    private void attemptUpbitCompensationBuy(TradeRequestDto tradeRequestDto, BinanceFuturesOrderResponseDto binanceOrderResponse) {
        try {
            BigDecimal upbitPrice = getCurrentUpbitPrice(tradeRequestDto.getUpbitSymbol());
            BigDecimal compensationAmount = new BigDecimal(binanceOrderResponse.getExecutedQty()).multiply(upbitPrice);
            
            UpbitOrderRequestDto compensateRequest = UpbitOrderRequestDto.builder()
                    .market(tradeRequestDto.getUpbitSymbol())
                    .price(compensationAmount)
                    .ordType(UpbitOrderType.MARKET)
                    .side("bid") // 매수
                    .build();
            
            upbitApiClient.createOrder(compensateRequest);
            log.info("업비트 보상 매수 주문 성공. Market: {}, Amount: {}", tradeRequestDto.getUpbitSymbol(), compensationAmount);
        } catch (Exception e) {
            log.error("업비트 보상 매수 주문 실패: {}", e.getMessage());
            // TODO: 보상 매수 실패시 큐에 저장하는 로직 추가
        }
    }

    private void attemptBinanceCompensationClose(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrderResponse) {
        try {
            BigDecimal upbitVolume = upbitOrderResponse.getVolume();
            BigDecimal binancePrice = getCurrentBinancePrice(tradeRequestDto.getBinanceSymbol());
            BigDecimal usdKrwRate = getCurrentUsdKrwRate();
            
            // 업비트 매도량을 바이낸스 수량으로 환산
            BigDecimal upbitPrice = getCurrentUpbitPrice(tradeRequestDto.getUpbitSymbol());
            BigDecimal krwAmount = upbitVolume.multiply(upbitPrice);
            BigDecimal usdAmount = krwAmount.divide(usdKrwRate, 2, java.math.RoundingMode.DOWN);
            BigDecimal binanceQuantity = usdAmount.multiply(tradeRequestDto.getLeverage()).divide(binancePrice, 8, java.math.RoundingMode.DOWN);
            
            BinanceFuturesOrderResponseDto closeOrderResponse = binanceFuturesApiClient.closePosition(tradeRequestDto.getBinanceSymbol(), binanceQuantity);
            log.info("바이낸스 보상 포지션 정리 성공: Symbol={}, Quantity={}, OrderId={}", 
                    tradeRequestDto.getBinanceSymbol(), binanceQuantity, closeOrderResponse.getOrderId());
        } catch (Exception e) {
            log.error("바이낸스 보상 포지션 정리 실패: Symbol={}, Error={}", tradeRequestDto.getBinanceSymbol(), e.getMessage());
            // TODO: 보상 포지션 정리 실패시 큐에 저장하는 로직 추가
        }
    }

    // 거래 결과 처리 메소드들
    private void handleTradeResult(TradeRequestDto request, UpbitOrderResponseDto upbit, BinanceFuturesOrderResponseDto binance) {
        if (upbit == null && binance == null) {
            log.error("업비트, 바이낸스 모두 주문 실패");
            saveFailedOrderResult(request, null);
        } else if (upbit == null) {
            log.error("Upbit 주문 실패, Binance 주문 성공 - 바이낸스 포지션 정리 필요");
            saveFailedOrderResult(request, null);
            attemptBinancePositionClose(request.getBinanceSymbol(), binance);
        } else if (binance == null) {
            log.error("Binance 주문 실패, 보상 매도 시도");
            saveFailedOrderResult(request, upbit);
            attemptCompensation(upbit);
        } else {
            log.info("Upbit 매수 주문 성공: {}", upbit.getUuid());
            log.info("Binance 선물 숏 주문 성공: {}", binance.getOrderId());
            saveOrderResult(request, upbit, binance);
        }
    }

    private void handleCloseTradeResult(TradeRequestDto request, UpbitOrderResponseDto upbit, BinanceFuturesOrderResponseDto binance) {
        if (upbit == null && binance == null) {
            log.error("업비트, 바이낸스 모두 포지션 정리 실패");
            saveFailedCloseOrderResult(request, null);
        } else if (upbit == null) {
            log.error("Upbit 매도 실패, Binance 숏 정리 성공 - 업비트 보상 매수 필요");
            saveFailedCloseOrderResult(request, null);
            attemptUpbitCompensationBuy(request, binance);
        } else if (binance == null) {
            log.error("Binance 숏 정리 실패, 업비트 매도 성공 - 바이낸스 포지션 재정리 필요");
            saveFailedCloseOrderResult(request, upbit);
            attemptBinanceCompensationClose(request, upbit);
        } else {
            log.info("Upbit 매도 주문 성공: {}", upbit.getUuid());
            log.info("Binance 숏 정리 주문 성공: {}", binance.getOrderId());
            saveCloseOrderResult(request, upbit, binance);
        }
    }
}
