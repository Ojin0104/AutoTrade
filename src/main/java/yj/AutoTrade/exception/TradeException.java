package yj.AutoTrade.exception;

public class TradeException extends RuntimeException {

    private final String errorCode; // 코드도 유지하고
    private final String errorMessage; // 외부 API에서 받은 에러 메시지

    public TradeException(String errorCode, String errorMessage) {
        super(errorMessage); // Exception 메시지에도 저장
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
