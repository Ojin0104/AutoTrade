package yj.AutoTrade.binance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
@JsonIgnoreProperties(ignoreUnknown = true)
public record BinancePriceResponseDto (
    String symbol,
    BigDecimal markPrice,
    BigDecimal indexPrice,
    BigDecimal estimatedSettlePrice,
    BigDecimal lastFundingRate,
    BigDecimal interestRate,
    Long nextFundingTime,
    Long time
) {}

