package yj.AutoTrade.upbit;

import yj.AutoTrade.exception.TradeException;

public class UpbitException extends TradeException {

    public UpbitException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
