package yj.AutoTrade.kimp.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class KimpStatisticsDto {
    private final String upbitSymbol;
    private final String binanceSymbol;
    private final BigDecimal averageKimp;
    private final BigDecimal maxKimp;
    private final BigDecimal minKimp;
    private final BigDecimal volatility;
    private final Long dataCount;
    private final LocalDateTime periodStart;
    private final LocalDateTime periodEnd;
    private final Long profitableCount;
    private final BigDecimal profitableRatio;
}