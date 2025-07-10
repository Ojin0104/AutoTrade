package yj.AutoTrade.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yj.AutoTrade.common.ApiResponse;
import yj.AutoTrade.upbit.UpbitException;
import yj.AutoTrade.binance.BinanceException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TradeException.class)
    public ResponseEntity<ApiResponse<Void>> handleTradeException(TradeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(ex.getErrorCode(), ex.getErrorMessage(), null));
    }

    @ExceptionHandler(UpbitException.class)
    public ResponseEntity<ApiResponse<Void>> handleUpbitException(UpbitException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(ex.getErrorCode(), ex.getMessage(), null));
    }

    @ExceptionHandler(BinanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleBinanceException(BinanceException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(ex.getErrorCode(), ex.getMessage(), null));
    }

}
