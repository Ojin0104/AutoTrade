package yj.AutoTrade.autotrade.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import yj.AutoTrade.autotrade.entity.AutoTradeConfig;
import yj.AutoTrade.autotrade.repository.AutoTradeConfigRepository;
import yj.AutoTrade.trade.TradeService;
import yj.AutoTrade.trade.dto.TradeRequestDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoTradeWriter implements ItemWriter<AutoTradeConfig> {

    private final TradeService tradeService;
    private final AutoTradeConfigRepository autoTradeConfigRepository;

    @Override
    public void write(Chunk<? extends AutoTradeConfig> chunk) throws Exception {
        for (AutoTradeConfig config : chunk) {
            try {
                executeAutoTrade(config);
                updateConfigAfterTrade(config);
            } catch (Exception e) {
                log.error("자동매매 실행 실패: Symbol={}, Error={}", config.getUpbitSymbol(), e.getMessage());
                handleTradeFailure(config, e);
            }
        }
    }

    private void executeAutoTrade(AutoTradeConfig config) {
        log.info("자동매매 실행: Symbol={}, 시드={}", config.getUpbitSymbol(), config.getSeedAmount());

        TradeRequestDto tradeRequest = TradeRequestDto.builder()
                .upbitSymbol(config.getUpbitSymbol())
                .binanceSymbol(config.getBinanceSymbol())
                .price(config.getSeedAmount())
                .leverage(config.getLeverage())
                .build();

        tradeService.trade(tradeRequest);
        log.info("자동매매 실행 완료: Symbol={}, 시드={}", config.getUpbitSymbol(), config.getSeedAmount());
    }

    private void updateConfigAfterTrade(AutoTradeConfig config) {
        config.incrementDailyTrades();
        autoTradeConfigRepository.save(config);
        log.info("자동매매 설정 업데이트: Symbol={}, 일일거래횟수={}/{}", 
                config.getUpbitSymbol(), config.getCurrentDailyTrades(), config.getMaxDailyTrades());
    }

    private void handleTradeFailure(AutoTradeConfig config, Exception e) {
        // 실패 처리 로직 (예: 실패 카운트 증가, 알림 등)
        log.error("자동매매 실패 처리: Symbol={}, 에러={}", config.getUpbitSymbol(), e.getMessage());
    }
}