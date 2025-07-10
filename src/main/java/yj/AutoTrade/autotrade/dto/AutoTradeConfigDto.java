package yj.AutoTrade.autotrade.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AutoTradeConfigDto {
    
    private final String upbitSymbol;
    private final String binanceSymbol;
    
    private final BigDecimal kimpThreshold;
    private final BigDecimal totalAmount;
    private final BigDecimal leverage;
    
    private final int splitCount;
    private final long intervalSeconds;
    
    private final List<BigDecimal> splitRatios;
    
    private final boolean isActive;
    private final BigDecimal maxDailyLoss;
    private final int maxDailyTrades;
}