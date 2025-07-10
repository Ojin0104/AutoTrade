package yj.AutoTrade.autotrade.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yj.AutoTrade.autotrade.entity.AutoTradeConfig;
import yj.AutoTrade.autotrade.repository.AutoTradeConfigRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AutoTradeConfigService {

    private final AutoTradeConfigRepository autoTradeConfigRepository;

    public List<AutoTradeConfig> getAllConfigs() {
        return autoTradeConfigRepository.findAll();
    }

    public List<AutoTradeConfig> getActiveConfigs() {
        return autoTradeConfigRepository.findByIsActiveTrue();
    }

    public AutoTradeConfig createConfig(AutoTradeConfig config) {
        // 기본값 설정
        if (config.getCurrentDailyTrades() == null) {
            config.setCurrentDailyTrades(0);
        }
        if (config.getCurrentDailyLoss() == null) {
            config.setCurrentDailyLoss(BigDecimal.ZERO);
        }
        if (config.getIsActive() == null) {
            config.setIsActive(true);
        }
        
        return autoTradeConfigRepository.save(config);
    }

    public AutoTradeConfig updateConfig(Long id, AutoTradeConfig config) {
        AutoTradeConfig existingConfig = autoTradeConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("자동매매 설정을 찾을 수 없습니다: " + id));

        existingConfig.setUpbitSymbol(config.getUpbitSymbol());
        existingConfig.setBinanceSymbol(config.getBinanceSymbol());
        existingConfig.setSeedAmount(config.getSeedAmount());
        existingConfig.setKimpThreshold(config.getKimpThreshold());
        existingConfig.setLeverage(config.getLeverage());
        existingConfig.setMaxDailyTrades(config.getMaxDailyTrades());
        existingConfig.setMaxDailyLoss(config.getMaxDailyLoss());
        existingConfig.setDescription(config.getDescription());

        return autoTradeConfigRepository.save(existingConfig);
    }

    public void toggleConfig(Long id) {
        AutoTradeConfig config = autoTradeConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("자동매매 설정을 찾을 수 없습니다: " + id));

        config.setIsActive(!config.getIsActive());
        autoTradeConfigRepository.save(config);
        
        log.info("자동매매 설정 토글: ID={}, 활성화={}", id, config.getIsActive());
    }

    public void deleteConfig(Long id) {
        autoTradeConfigRepository.deleteById(id);
        log.info("자동매매 설정 삭제: ID={}", id);
    }

    public void resetDailyCounters() {
        List<AutoTradeConfig> configs = autoTradeConfigRepository.findAll();
        for (AutoTradeConfig config : configs) {
            config.resetDailyCounters();
        }
        autoTradeConfigRepository.saveAll(configs);
        log.info("모든 자동매매 설정의 일일 카운터 리셋 완료: {} 개", configs.size());
    }
}