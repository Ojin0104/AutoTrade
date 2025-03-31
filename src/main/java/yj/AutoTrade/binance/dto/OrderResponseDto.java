package yj.AutoTrade.binance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponseDto(

        // 공통 필드 (ACK, RESULT, FULL)
        String symbol,
        Long orderId,
        Long orderListId,
        String clientOrderId,
        Long transactTime,

        // RESULT 이상에서 나오는 필드
        BigDecimal price,
        BigDecimal origQty,
        BigDecimal executedQty,
        BigDecimal origQuoteOrderQty,
        BigDecimal cummulativeQuoteQty,
        String status,
        TimeInForce timeInForce,
        OrderType type,
        OrderSide side,
        Long workingTime,
        SelfTradePreventionMode selfTradePreventionMode,

        // FULL에서만 등장
        List<Fill> fills,

        // 조건부 필드들
        BigDecimal icebergQty,
        Long preventedMatchId,
        BigDecimal preventedQuantity,
        BigDecimal stopPrice,
        Long strategyId,
        Integer strategyType,
        Long trailingDelta,
        Long trailingTime,
        Boolean usedSor,
        String workingFloor

) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Fill(
            BigDecimal price,
            BigDecimal qty,
            BigDecimal commission,
            String commissionAsset,
            Long tradeId
    ) {}
}
