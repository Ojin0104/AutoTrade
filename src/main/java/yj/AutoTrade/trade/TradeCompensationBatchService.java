package yj.AutoTrade.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yj.AutoTrade.trade.entity.TradeCompensationQueue;
import yj.AutoTrade.trade.entity.TradeCompensationStatus;
import yj.AutoTrade.trade.repository.TradeCompensationQueueRepository;
import yj.AutoTrade.upbit.UpbitApiClient;
import yj.AutoTrade.upbit.UpbitException;
import yj.AutoTrade.upbit.dto.UpbitOrderRequestDto;
import yj.AutoTrade.upbit.dto.UpbitOrderResponseDto;
import yj.AutoTrade.upbit.dto.UpbitOrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeCompensationBatchService {

    private final TradeCompensationQueueRepository tradeCompensationQueueRepository;
    private final UpbitApiClient upbitApiClient;
    
    private static final int MAX_RETRY_COUNT = 3; // 최대 재시도 횟수
    
    /**
     * 5분마다 PENDING 상태인 보상 큐를 처리
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void processCompensationQueue() {
        log.info("보상 큐 배치 처리 시작");
        
        try {
            // 재시도 횟수가 제한 이하인 PENDING 상태 조회
            List<TradeCompensationQueue> pendingCompensations = 
                tradeCompensationQueueRepository.findByStatusAndRetryCountLessThan(
                    TradeCompensationStatus.PENDING, MAX_RETRY_COUNT);
            
            if (pendingCompensations.isEmpty()) {
                log.info("처리할 보상 큐가 없습니다.");
                return;
            }
            
            log.info("처리할 보상 큐 개수: {}", pendingCompensations.size());
            
            for (TradeCompensationQueue compensation : pendingCompensations) {
                processCompensation(compensation);
            }
            
        } catch (Exception e) {
            log.error("보상 큐 배치 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 개별 보상 처리
     */
    private void processCompensation(TradeCompensationQueue compensation) {
        try {
            log.info("보상 처리 시작 - ID: {}, Upbit 주문 ID: {}", 
                    compensation.getId(), compensation.getUpbitOrderId());
            
            // 매도 주문 생성
            UpbitOrderRequestDto sellOrderRequest = UpbitOrderRequestDto.builder()
                    .market(compensation.getSymbol())
                    .volume(new BigDecimal(compensation.getQuantity()))
                    .price(new BigDecimal(compensation.getPrice()))
                    .ordType(UpbitOrderType.LIMIT)
                    .side("ask") // 매도
                    .build();
            
            UpbitOrderResponseDto response = upbitApiClient.createOrder(sellOrderRequest);
            
            // 성공 시 상태 업데이트
            compensation.setStatus(TradeCompensationStatus.SUCCESS);
            compensation.setErrorMessage(null);
            tradeCompensationQueueRepository.save(compensation);
            
            log.info("보상 처리 성공 - ID: {}, Upbit 주문 ID: {}, 보상 주문 ID: {}", 
                    compensation.getId(), compensation.getUpbitOrderId(), response.getUuid());
            
        } catch (UpbitException e) {
            handleCompensationFailure(compensation, e);
        } catch (Exception e) {
            log.error("보상 처리 중 예상치 못한 오류 - ID: {}, 오류: {}", 
                    compensation.getId(), e.getMessage(), e);
            handleCompensationFailure(compensation, e);
        }
    }
    
    /**
     * 보상 처리 실패 시 처리
     */
    private void handleCompensationFailure(TradeCompensationQueue compensation, Exception e) {
        compensation.setRetryCount(compensation.getRetryCount() + 1);
        compensation.setLastAttemptAt(LocalDateTime.now());
        
        if (compensation.getRetryCount() >= MAX_RETRY_COUNT) {
            compensation.setStatus(TradeCompensationStatus.FAILED);
            log.error("보상 처리 최대 재시도 횟수 초과 - ID: {}, Upbit 주문 ID: {}, 오류: {}", 
                    compensation.getId(), compensation.getUpbitOrderId(), e.getMessage());
        } else {
            log.warn("보상 처리 실패, 재시도 예정 - ID: {}, 재시도 횟수: {}, 오류: {}", 
                    compensation.getId(), compensation.getRetryCount(), e.getMessage());
        }
        
        compensation.setErrorMessage(e.getMessage());
        tradeCompensationQueueRepository.save(compensation);
    }
} 