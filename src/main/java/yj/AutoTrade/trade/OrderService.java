package yj.AutoTrade.trade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yj.AutoTrade.binance.dto.BinanceFuturesOrderResponseDto;
import yj.AutoTrade.binance.dto.BinanceOrderResponseDto;
import yj.AutoTrade.trade.dto.TradeRequestDto;
import yj.AutoTrade.trade.entity.Order;
import yj.AutoTrade.trade.entity.OrderStatus;
import yj.AutoTrade.trade.repository.OrderRepository;
import yj.AutoTrade.upbit.dto.UpbitOrderResponseDto;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveSuccessfulOrder(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrder, BinanceOrderResponseDto binanceOrder) {
        Order order = Order.builder()
                .upbitOrderId(upbitOrder.getUuid())
                .binanceOrderId(binanceOrder.orderId().toString())
                .upbitSymbol(tradeRequestDto.getUpbitSymbol())
                .binanceSymbol(tradeRequestDto.getBinanceSymbol())
                .status(OrderStatus.OPEN)
                .quantity(upbitOrder.getVolume())
                .upbitAvgPrice(upbitOrder.getPrice())
                .binanceAvgPrice(binanceOrder.price())
                .leverage(tradeRequestDto.getLeverage())
                .build();
        return orderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveSuccessfulOrder(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrder, BinanceFuturesOrderResponseDto binanceOrder) {
        Order order = Order.builder()
                .upbitOrderId(upbitOrder.getUuid())
                .binanceOrderId(binanceOrder.getOrderId().toString())
                .upbitSymbol(tradeRequestDto.getUpbitSymbol())
                .binanceSymbol(tradeRequestDto.getBinanceSymbol())
                .status(OrderStatus.OPEN)
                .quantity(upbitOrder.getVolume())
                .upbitAvgPrice(upbitOrder.getPrice())
                .binanceAvgPrice(new BigDecimal(binanceOrder.getAvgPrice()))
                .leverage(tradeRequestDto.getLeverage())
                .build();
        return orderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveFailedOrder(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrder) {
        Order.OrderBuilder builder = Order.builder()
                .upbitSymbol(tradeRequestDto.getUpbitSymbol())
                .binanceSymbol(tradeRequestDto.getBinanceSymbol())
                .status(OrderStatus.FAILED)
                .leverage(tradeRequestDto.getLeverage());

        if (upbitOrder != null) {
            builder.upbitOrderId(upbitOrder.getUuid())
                    .quantity(upbitOrder.getVolume())
                    .upbitAvgPrice(upbitOrder.getPrice());
        }
        
        return orderRepository.save(builder.build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveCloseOrder(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrder, BinanceFuturesOrderResponseDto binanceOrder) {
        Order order = Order.builder()
                .upbitOrderId(upbitOrder.getUuid())
                .binanceOrderId(binanceOrder.getOrderId().toString())
                .upbitSymbol(tradeRequestDto.getUpbitSymbol())
                .binanceSymbol(tradeRequestDto.getBinanceSymbol())
                .status(OrderStatus.CLOSED)
                .quantity(upbitOrder.getVolume())
                .upbitAvgPrice(upbitOrder.getPrice())
                .binanceAvgPrice(new BigDecimal(binanceOrder.getAvgPrice()))
                .leverage(tradeRequestDto.getLeverage())
                .build();
        return orderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveFailedCloseOrder(TradeRequestDto tradeRequestDto, UpbitOrderResponseDto upbitOrder) {
        Order.OrderBuilder builder = Order.builder()
                .upbitSymbol(tradeRequestDto.getUpbitSymbol())
                .binanceSymbol(tradeRequestDto.getBinanceSymbol())
                .status(OrderStatus.CLOSE_FAILED)
                .leverage(tradeRequestDto.getLeverage());

        if (upbitOrder != null) {
            builder.upbitOrderId(upbitOrder.getUuid())
                    .quantity(upbitOrder.getVolume())
                    .upbitAvgPrice(upbitOrder.getPrice());
        }
        
        return orderRepository.save(builder.build());
    }
} 