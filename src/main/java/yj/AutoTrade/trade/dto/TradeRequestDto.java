package yj.AutoTrade.trade.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
@Builder
@Getter
public class TradeRequestDto {

    private String upbitSymbol;
    private String binanceSymbol;
    private BigDecimal volume;
    private BigDecimal price;
    private BigDecimal leverage;
}
