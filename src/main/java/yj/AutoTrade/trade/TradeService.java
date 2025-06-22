package yj.AutoTrade.trade;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import yj.AutoTrade.upbit.UpbitApiClient;
import yj.AutoTrade.binance.BinanceApiClient;
import java.math.BigDecimal;
import yj.AutoTrade.upbit.dto.UpbitOrderRequestDto;
import yj.AutoTrade.binance.dto.BinanceOrderRequestDto;
@Service
@RequiredArgsConstructor
public class TradeService {

    private final UpbitApiClient upbitApiClient;
    private final BinanceApiClient binanceApiClient;

    
    public void trade(String upbitSymbol, String binanceSymbol, BigDecimal amount) {

        UpbitOrderRequestDto upbitOrderRequestDto = UpbitOrderRequestDto.builder()
        .market(binanceSymbol)
        .price(binanceSymbol)
        .build();

        //upbitApiClient.createOrder(upbitOrderRequestDto);

        BinanceOrderRequestDto binanceOrderRequestDto = BinanceOrderRequestDto.builder()
        .symbol(upbitSymbol)
        .quantity(amount)
        .build();

        //binanceApiClient.createOrder(binanceOrderRequestDto);
    }
}
