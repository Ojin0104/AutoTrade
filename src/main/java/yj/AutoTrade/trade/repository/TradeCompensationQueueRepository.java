package yj.AutoTrade.trade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yj.AutoTrade.trade.entity.TradeCompensationQueue;
import yj.AutoTrade.trade.entity.TradeCompensationStatus;

import java.util.List;

@Repository
public interface TradeCompensationQueueRepository extends JpaRepository<TradeCompensationQueue, Long> {
    
    // PENDING 상태인 보상 큐 조회
    List<TradeCompensationQueue> findByStatus(TradeCompensationStatus status);
    
    // 재시도 횟수가 제한 이하인 PENDING 상태 조회
    List<TradeCompensationQueue> findByStatusAndRetryCountLessThan(TradeCompensationStatus status, int maxRetryCount);
} 