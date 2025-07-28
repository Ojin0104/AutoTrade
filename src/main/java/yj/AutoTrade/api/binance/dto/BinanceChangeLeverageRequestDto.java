package yj.AutoTrade.api.binance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BinanceChangeLeverageRequestDto {
    private String symbol;
    private Integer leverage;
    private Long recvWindow;
    private Long timestamp;
} 