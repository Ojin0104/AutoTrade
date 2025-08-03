package yj.AutoTrade.api.upbit;

import yj.AutoTrade.exception.ErrorCode;
import yj.AutoTrade.exception.TradeException;

public class UpbitException extends TradeException {

    public UpbitException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UpbitException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
