package yj.AutoTrade.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yj.AutoTrade.trade.entity.TradeCompensationQueue;
import yj.AutoTrade.trade.entity.TradeCompensationStatus;
import yj.AutoTrade.trade.repository.TradeCompensationQueueRepository;
import yj.AutoTrade.upbit.UpbitException;
import yj.AutoTrade.upbit.dto.UpbitOrderResponseDto;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeCompensationQueueService {

    private final TradeCompensationQueueRepository tradeCompensationQueueRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToQueue(UpbitOrderResponseDto upbitOrderResponse, UpbitException compensationException) {
        try {
            TradeCompensationQueue compensationQueue = TradeCompensationQueue.builder()
                    .upbitOrderId(upbitOrderResponse.getUuid())
                    .symbol(upbitOrderResponse.getMarket())
                    .quantity(upbitOrderResponse.getVolume())
                    .price(upbitOrderResponse.getPrice())
                    .status(TradeCompensationStatus.PENDING)
                    .errorMessage(compensationException.getMessage())
                    .retryCount(0)
                    .lastAttemptAt(LocalDateTime.now())
                    .build();
            tradeCompensationQueueRepository.save(compensationQueue);
            log.info("보상 큐에 저장 완료 - Upbit 주문 ID: {}", upbitOrderResponse.getUuid());
        } catch (Exception e) {
            log.error("보상 큐 저장 실패: {}", e.getMessage(), e);
        }
    }
} 