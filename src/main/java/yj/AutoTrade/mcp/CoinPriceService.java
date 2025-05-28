package yj.AutoTrade.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import yj.AutoTrade.binance.BinanceApiClient;
import yj.AutoTrade.upbit.UpbitApiClient;
import yj.AutoTrade.upbit.dto.UpbitTickerResponseDto;

@Service
@RequiredArgsConstructor
public class CoinPriceService {

    private final UpbitApiClient upbitApiClient;
    private final BinanceApiClient binanceApiClient;

    @Tool(description = "Get coin price information")
    public UpbitTickerResponseDto getWeatherForecastByLocation(
            String market   // coin Id
    ) {
        return upbitApiClient.getUpbitTicker(market)[0];
    }
}
