package yj.AutoTrade.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TradeException.class)
    public ResponseEntity<ErrorResponse> handleTradeException(TradeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getErrorMessage()));
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}
