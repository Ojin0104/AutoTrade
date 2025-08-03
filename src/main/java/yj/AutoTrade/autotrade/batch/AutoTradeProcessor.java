package yj.AutoTrade.autotrade.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import yj.AutoTrade.autotrade.entity.AutoTradeConfig;
import yj.AutoTrade.trade.PriceGapCalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoTradeProcessor implements ItemProcessor<AutoTradeConfig, AutoTradeConfig> {

    private final PriceGapCalculator priceGapCalculator;

    @Override
    public AutoTradeConfig process(AutoTradeConfig config) throws Exception {
        log.info("자동매매 설정 처리 시작: Symbol={}, 임계값={}%", 
                config.getUpbitSymbol(), config.getKimpThreshold());

        // 1. 거래 실행 가능 여부 확인
        if (!config.canExecuteTrade()) {
            log.info("거래 실행 불가: 일일 한도 초과 또는 비활성화 상태");
            return null;
        }

        // 2. 현재 김프 계산
        BigDecimal currentKimp = calculateCurrentKimp(config);
        log.info("현재 김프: {}%, 임계값: {}%", currentKimp, config.getKimpThreshold());

        // 3. 김프 조건 확인
        if (currentKimp.compareTo(config.getKimpThreshold()) < 0) {
            log.info("김프 조건 미충족: 현재={}%, 임계값={}%", currentKimp, config.getKimpThreshold());
            return null;
        }

        // 4. 거래 실행 대상으로 설정
        config.setLastExecutedAt(LocalDateTime.now());
        log.info("자동매매 실행 대상 설정: Symbol={}, 시드={}, 김프={}%", 
                config.getUpbitSymbol(), config.getSeedAmount(), currentKimp);

        return config;
    }

    private BigDecimal calculateCurrentKimp(AutoTradeConfig config) {
        try {
            return priceGapCalculator.calculatePremium(
                    config.getUpbitSymbol(), 
                    config.getBinanceSymbol()
            );
        } catch (Exception e) {
            log.error("김프 계산 실패: Symbol={}, Error={}", config.getUpbitSymbol(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}