package yj.AutoTrade.binance;

import yj.AutoTrade.exception.TradeException;

public class BinanceException extends TradeException {

    public BinanceException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
