package yj.AutoTrade.trade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yj.AutoTrade.binance.BinanceApiClient;
import yj.AutoTrade.upbit.UpbitApiClient;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class PriceGapCalculator {
    private final UpbitApiClient upbitApiClient;
    private final BinanceApiClient binanceApiClient;

    public BigDecimal calculatePremium(String upbitSymbol, String binanceSymbol) {

        BigDecimal upbitPrice = BigDecimal.valueOf(upbitApiClient.getUpbitTicker(upbitSymbol)[0].getTradePrice());
        BigDecimal binancePrice = BigDecimal.valueOf(binanceApiClient.getTickerPrice(binanceSymbol).getPrice());

        BigDecimal premium = upbitPrice.subtract(binancePrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(binancePrice, 2, RoundingMode.HALF_UP);System.out.println("premium: " + premium);
        return premium;
    }

}
