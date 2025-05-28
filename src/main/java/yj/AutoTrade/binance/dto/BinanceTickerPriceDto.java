package yj.AutoTrade.binance.dto;

import lombok.Data;

@Data
public class BinanceTickerPriceDto {
    private String symbol;
    private Double price;
} 