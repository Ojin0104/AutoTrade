package yj.AutoTrade.api.binance;

import yj.AutoTrade.exception.ErrorCode;
import yj.AutoTrade.exception.TradeException;

public class BinanceException extends TradeException {

    public BinanceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BinanceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
