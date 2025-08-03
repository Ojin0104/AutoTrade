package yj.AutoTrade.api.exchange;

import yj.AutoTrade.exception.ErrorCode;
import yj.AutoTrade.exception.TradeException;

public class ExchangeRateException extends TradeException {

    public ExchangeRateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExchangeRateException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}