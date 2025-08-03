package yj.AutoTrade.kimp.dto;

import lombok.Builder;
import lombok.Getter;
import yj.AutoTrade.kimp.entity.CollectionStatus;
import yj.AutoTrade.kimp.entity.KimpHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class KimpDataDto {
    private final String upbitSymbol;
    private final String binanceSymbol;
    private final String coinName;
    private final BigDecimal upbitPrice;
    private final BigDecimal binancePrice;
    private final BigDecimal binancePriceKrw;
    private final BigDecimal usdtKrwRate;
    private final BigDecimal kimpRate;
    private final BigDecimal priceDifference;
    private final BigDecimal upbitVolume24h;
    private final BigDecimal binanceVolume24h;
    private final LocalDateTime collectedAt;
    private final CollectionStatus status;
    private final String errorMessage;

    public static KimpDataDto from(KimpHistory kimpHistory) {
        return KimpDataDto.builder()
                .upbitSymbol(kimpHistory.getUpbitSymbol())
                .binanceSymbol(kimpHistory.getBinanceSymbol())
                .upbitPrice(kimpHistory.getUpbitPrice())
                .binancePrice(kimpHistory.getBinancePrice())
                .binancePriceKrw(kimpHistory.getBinancePriceKrw())
                .usdtKrwRate(kimpHistory.getUsdtKrwRate())
                .kimpRate(kimpHistory.getKimpRate())
                .priceDifference(kimpHistory.getPriceDifference())
                .upbitVolume24h(kimpHistory.getUpbitVolume24h())
                .binanceVolume24h(kimpHistory.getBinanceVolume24h())
                .collectedAt(kimpHistory.getCollectedAt())
                .status(kimpHistory.getStatus())
                .errorMessage(kimpHistory.getErrorMessage())
                .build();
    }

    public static KimpDataDto from(KimpHistory kimpHistory, String coinName) {
        return KimpDataDto.builder()
                .upbitSymbol(kimpHistory.getUpbitSymbol())
                .binanceSymbol(kimpHistory.getBinanceSymbol())
                .coinName(coinName)
                .upbitPrice(kimpHistory.getUpbitPrice())
                .binancePrice(kimpHistory.getBinancePrice())
                .binancePriceKrw(kimpHistory.getBinancePriceKrw())
                .usdtKrwRate(kimpHistory.getUsdtKrwRate())
                .kimpRate(kimpHistory.getKimpRate())
                .priceDifference(kimpHistory.getPriceDifference())
                .upbitVolume24h(kimpHistory.getUpbitVolume24h())
                .binanceVolume24h(kimpHistory.getBinanceVolume24h())
                .collectedAt(kimpHistory.getCollectedAt())
                .status(kimpHistory.getStatus())
                .errorMessage(kimpHistory.getErrorMessage())
                .build();
    }

    public boolean isPositiveKimp() {
        return kimpRate.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getAbsoluteKimpRate() {
        return kimpRate.abs();
    }

    public boolean isProfitable(BigDecimal threshold) {
        return getAbsoluteKimpRate().compareTo(threshold) >= 0;
    }
}