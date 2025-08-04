package yj.AutoTrade.autotrade.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yj.AutoTrade.autotrade.entity.AutoTradeConfig;
import yj.AutoTrade.autotrade.repository.AutoTradeConfigRepository;
import yj.AutoTrade.trade.PriceGapCalculator;
import yj.AutoTrade.trade.TradeService;
import yj.AutoTrade.trade.dto.TradeRequestDto;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoTradeScheduler {

    private final AutoTradeConfigRepository autoTradeConfigRepository;
    private final PriceGapCalculator priceGapCalculator;
    private final TradeService tradeService;

    /**
     * 자동매매 스케줄러 - 매 10분마다 실행
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void runAutoTrade() {
        try {
            log.info("자동매매 스케줄러 시작");
            
            // 1. 실행 가능한 설정들 조회
            List<AutoTradeConfig> configs = autoTradeConfigRepository.findExecutableConfigs();
            
            if (configs.isEmpty()) {
                log.info("실행 가능한 자동매매 설정이 없습니다.");
                return;
            }
            
            log.info("실행 가능한 자동매매 설정 수: {}", configs.size());
            
            // 2. 각 설정에 대해 처리
            for (AutoTradeConfig config : configs) {
                processAutoTradeConfig(config);
            }
            
            log.info("자동매매 스케줄러 완료");
            
        } catch (Exception e) {
            log.error("자동매매 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 개별 자동매매 설정 처리
     */
    private void processAutoTradeConfig(AutoTradeConfig config) {
        try {
            log.info("자동매매 설정 처리 시작: {} ({})", config.getDescription(), config.getId());
            
            // 1. 일일 거래 한도 확인
            if (config.getCurrentDailyTrades() >= config.getMaxDailyTrades()) {
                log.info("일일 거래 한도 초과: {} >= {}", config.getCurrentDailyTrades(), config.getMaxDailyTrades());
                return;
            }
            
            // 2. 김치프리미엄 계산
            BigDecimal currentPremium = priceGapCalculator.calculatePremium(
                config.getUpbitSymbol(), 
                config.getBinanceSymbol()
            );
            
            log.info("현재 김치프리미엄: {}%, 임계값: {}%", currentPremium, config.getKimpThreshold());
            
            // 3. 임계값 확인
            if (currentPremium.compareTo(config.getKimpThreshold()) <= 0) {
                log.info("김치프리미엄이 임계값 이하입니다. 거래하지 않습니다.");
                return;
            }
            
            // 4. 거래 실행
            TradeRequestDto tradeRequest = TradeRequestDto.builder()
                .upbitSymbol(config.getUpbitSymbol())
                .binanceSymbol(config.getBinanceSymbol())
                .price(config.getSeedAmount())
                .leverage(config.getLeverage())
                .build();
                
            log.info("자동매매 거래 실행: {} -> {}, 금액: {}, 레버리지: {}", 
                config.getUpbitSymbol(), config.getBinanceSymbol(), 
                config.getSeedAmount(), config.getLeverage());
                
            tradeService.trade(tradeRequest);
            
            // 5. 거래 카운트 증가
            config.incrementDailyTrades();
            config.setLastExecutedAt(java.time.LocalDateTime.now());
            autoTradeConfigRepository.save(config);
            
            log.info("자동매매 설정 처리 완료: {} (거래 카운트: {})", 
                config.getDescription(), config.getCurrentDailyTrades());
                
        } catch (Exception e) {
            log.error("자동매매 설정 처리 중 오류 발생: {} - {}", config.getDescription(), e.getMessage(), e);
        }
    }
}