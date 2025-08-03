package yj.AutoTrade.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yj.AutoTrade.common.ApiResponse;
import yj.AutoTrade.api.upbit.UpbitException;
import yj.AutoTrade.api.binance.BinanceException;
import yj.AutoTrade.api.exchange.ExchangeRateException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TradeException.class)
    public ResponseEntity<ApiResponse<Void>> handleTradeException(TradeException ex) {
        log.error("TradeException 발생: {} - {}", ex.getErrorCodeValue(), ex.getErrorMessage(), ex);
        return ResponseEntity
                .status(getHttpStatusByErrorCode(ex.getErrorCode()))
                .body(new ApiResponse<>(ex.getErrorCodeValue(), ex.getErrorMessage(), null));
    }

    @ExceptionHandler(UpbitException.class)
    public ResponseEntity<ApiResponse<Void>> handleUpbitException(UpbitException ex) {
        log.error("UpbitException 발생: {} - {}", ex.getErrorCodeValue(), ex.getErrorMessage(), ex);
        return ResponseEntity
                .status(getHttpStatusByErrorCode(ex.getErrorCode()))
                .body(new ApiResponse<>(ex.getErrorCodeValue(), ex.getErrorMessage(), null));
    }

    @ExceptionHandler(BinanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleBinanceException(BinanceException ex) {
        log.error("BinanceException 발생: {} - {}", ex.getErrorCodeValue(), ex.getErrorMessage(), ex);
        return ResponseEntity
                .status(getHttpStatusByErrorCode(ex.getErrorCode()))
                .body(new ApiResponse<>(ex.getErrorCodeValue(), ex.getErrorMessage(), null));
    }

    @ExceptionHandler(ExchangeRateException.class)
    public ResponseEntity<ApiResponse<Void>> handleExchangeRateException(ExchangeRateException ex) {
        log.error("ExchangeRateException 발생: {} - {}", ex.getErrorCodeValue(), ex.getErrorMessage(), ex);
        return ResponseEntity
                .status(getHttpStatusByErrorCode(ex.getErrorCode()))
                .body(new ApiResponse<>(ex.getErrorCodeValue(), ex.getErrorMessage(), null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException 발생: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(ErrorCode.COMMON_SYSTEM_ERROR.getCode(), ErrorCode.COMMON_SYSTEM_ERROR.getMessage(), null));
    }

    /**
     * ErrorCode에 따른 HTTP 상태 코드 매핑
     */
    private HttpStatus getHttpStatusByErrorCode(ErrorCode errorCode) {
        String code = errorCode.getCode();
        
        // 인증/권한 에러
        if (code.startsWith("A")) {
            return HttpStatus.UNAUTHORIZED;
        }
        // 데이터 없음
        if (code.equals("C003") || code.equals("T005") || code.equals("P000") || code.equals("K001")) {
            return HttpStatus.NOT_FOUND;
        }
        // 중복 데이터
        if (code.equals("C004") || code.equals("A005") || code.equals("A006") || code.equals("P001")) {
            return HttpStatus.CONFLICT;
        }
        // 외부 서비스 에러
        if (code.startsWith("S") || code.startsWith("U") || code.startsWith("B") || code.startsWith("E")) {
            return HttpStatus.BAD_GATEWAY;
        }
        // 시스템 에러
        if (code.startsWith("D") || code.equals("C000")) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        // 기본값
        return HttpStatus.BAD_REQUEST;
    }
}
