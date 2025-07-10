package yj.AutoTrade.autotrade.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SplitTradeRequestDto {
    
    private final String upbitSymbol;
    private final String binanceSymbol;
    private final BigDecimal amount;
    private final BigDecimal leverage;
    private final int orderIndex;
    private final int totalOrders;
    private final LocalDateTime scheduledTime;
    private final BigDecimal currentKimp;
}