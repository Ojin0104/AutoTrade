package yj.AutoTrade.binance.dto;

import lombok.Data;

@Data
public class BinanceChangeLeverageResponseDto {
    private Integer leverage;
    private String maxNotionalValue;
    private String symbol;
} 