package yj.AutoTrade.api.binance.dto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BinanceFuturesAccountResponseDto(

        // 총계 (single-asset / multi-assets 공통)
        BigDecimal totalInitialMargin,
        BigDecimal totalMaintMargin,
        BigDecimal totalWalletBalance,
        BigDecimal totalUnrealizedProfit,
        BigDecimal totalMarginBalance,
        BigDecimal totalPositionInitialMargin,
        BigDecimal totalOpenOrderInitialMargin,
        BigDecimal totalCrossWalletBalance,
        BigDecimal totalCrossUnPnl,
        BigDecimal availableBalance,
        BigDecimal maxWithdrawAmount,

        // 자산 리스트
        List<Asset> assets,

        // 포지션 리스트
        List<Position> positions

) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Asset(
            String asset,
            BigDecimal walletBalance,
            BigDecimal unrealizedProfit,
            BigDecimal marginBalance,
            BigDecimal maintMargin,
            BigDecimal initialMargin,
            BigDecimal positionInitialMargin,
            BigDecimal openOrderInitialMargin,
            BigDecimal crossWalletBalance,
            BigDecimal crossUnPnl,
            BigDecimal availableBalance,
            BigDecimal maxWithdrawAmount,
            Long updateTime,

            // multi-assets mode에서만 존재
            Boolean marginAvailable
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Position(
            String symbol,
            String positionSide,
            BigDecimal positionAmt,
            BigDecimal unrealizedProfit,
            BigDecimal isolatedMargin,
            BigDecimal notional,
            BigDecimal isolatedWallet,
            BigDecimal initialMargin,
            BigDecimal maintMargin,
            Long updateTime
    ) {}
}
