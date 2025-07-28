package yj.AutoTrade.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    // === 공통 에러 (C000~C099) ===
    COMMON_SYSTEM_ERROR("C000", "시스템 오류가 발생했습니다"),
    COMMON_INVALID_PARAMETER("C001", "잘못된 파라미터입니다"),
    COMMON_INVALID_REQUEST("C002", "잘못된 요청입니다"),
    COMMON_NOT_FOUND("C003", "요청한 데이터를 찾을 수 없습니다"),
    COMMON_DUPLICATE_DATA("C004", "중복된 데이터입니다"),
    
    // === 인증/권한 에러 (A000~A099) ===
    AUTH_INVALID_TOKEN("A000", "유효하지 않은 토큰입니다"),
    AUTH_TOKEN_EXPIRED("A001", "토큰이 만료되었습니다"),
    AUTH_INSUFFICIENT_PERMISSION("A002", "권한이 부족합니다"),
    AUTH_INVALID_CREDENTIALS("A003", "잘못된 인증 정보입니다"),
    AUTH_USER_NOT_FOUND("A004", "사용자를 찾을 수 없습니다"),
    AUTH_DUPLICATE_EMAIL("A005", "이미 사용 중인 이메일입니다"),
    AUTH_DUPLICATE_USERNAME("A006", "이미 사용 중인 사용자명입니다"),
    
    // === 거래 에러 (T000~T099) ===
    TRADE_INSUFFICIENT_BALANCE("T000", "잔고가 부족합니다"),
    TRADE_ORDER_FAILED("T001", "주문 실행에 실패했습니다"),
    TRADE_INVALID_SYMBOL("T002", "유효하지 않은 심볼입니다"),
    TRADE_INVALID_AMOUNT("T003", "유효하지 않은 거래 금액입니다"),
    TRADE_INVALID_LEVERAGE("T004", "유효하지 않은 레버리지입니다"),
    TRADE_POSITION_NOT_FOUND("T005", "포지션을 찾을 수 없습니다"),
    TRADE_COMPENSATION_FAILED("T006", "보상 거래에 실패했습니다"),
    TRADE_BALANCE_VALIDATION_FAILED("T007", "잔고 검증에 실패했습니다"),
    
    // === 업비트 API 에러 (U000~U099) ===
    UPBIT_API_ERROR("U000", "업비트 API 오류가 발생했습니다"),
    UPBIT_AUTHENTICATION_ERROR("U001", "업비트 API 인증 오류입니다"),
    UPBIT_INSUFFICIENT_BALANCE("U002", "업비트 잔고가 부족합니다"),
    UPBIT_INVALID_SYMBOL("U003", "업비트에서 지원하지 않는 심볼입니다"),
    UPBIT_ORDER_ERROR("U004", "업비트 주문 처리 중 오류가 발생했습니다"),
    UPBIT_RATE_LIMIT("U005", "업비트 API 요청 한도를 초과했습니다"),
    UPBIT_NETWORK_ERROR("U006", "업비트 API 네트워크 오류입니다"),
    
    // === 바이낸스 API 에러 (B000~B099) ===
    BINANCE_API_ERROR("B000", "바이낸스 API 오류가 발생했습니다"),
    BINANCE_AUTHENTICATION_ERROR("B001", "바이낸스 API 인증 오류입니다"),
    BINANCE_INSUFFICIENT_BALANCE("B002", "바이낸스 잔고가 부족합니다"),
    BINANCE_INVALID_SYMBOL("B003", "바이낸스에서 지원하지 않는 심볼입니다"),
    BINANCE_ORDER_ERROR("B004", "바이낸스 주문 처리 중 오류가 발생했습니다"),
    BINANCE_LEVERAGE_ERROR("B005", "바이낸스 레버리지 설정 오류입니다"),
    BINANCE_POSITION_ERROR("B006", "바이낸스 포지션 처리 오류입니다"),
    BINANCE_RATE_LIMIT("B007", "바이낸스 API 요청 한도를 초과했습니다"),
    BINANCE_NETWORK_ERROR("B008", "바이낸스 API 네트워크 오류입니다"),
    
    // === 환율 API 에러 (E000~E099) ===
    EXCHANGE_RATE_API_ERROR("E000", "환율 API 호출에 실패했습니다"),
    EXCHANGE_RATE_INVALID_RESPONSE("E001", "환율 API 응답 데이터가 유효하지 않습니다"),
    EXCHANGE_RATE_SERVICE_UNAVAILABLE("E002", "환율 서비스를 이용할 수 없습니다"),
    EXCHANGE_RATE_TIMEOUT("E003", "환율 API 요청 시간이 초과되었습니다"),
    EXCHANGE_RATE_FALLBACK_FAILED("E004", "대체 환율 조회에도 실패했습니다"),
    
    // === 김프 데이터 에러 (K000~K099) ===
    KIMP_CALCULATION_ERROR("K000", "김프 계산 중 오류가 발생했습니다"),
    KIMP_DATA_NOT_FOUND("K001", "김프 데이터를 찾을 수 없습니다"),
    KIMP_PRICE_FETCH_ERROR("K002", "가격 정보 조회에 실패했습니다"),
    KIMP_INVALID_PERIOD("K003", "유효하지 않은 조회 기간입니다"),
    KIMP_COLLECTION_ERROR("K004", "김프 데이터 수집 중 오류가 발생했습니다"),
    
    // === 코인 페어 에러 (P000~P099) ===
    COIN_PAIR_NOT_FOUND("P000", "코인 페어를 찾을 수 없습니다"),
    COIN_PAIR_DUPLICATE("P001", "이미 존재하는 코인 페어입니다"),
    COIN_PAIR_INVALID_SYMBOL("P002", "유효하지 않은 코인 페어 심볼입니다"),
    COIN_PAIR_INACTIVE("P003", "비활성화된 코인 페어입니다"),
    
    // === 배치 처리 에러 (J000~J099) ===
    BATCH_JOB_ERROR("J000", "배치 작업 중 오류가 발생했습니다"),
    BATCH_ITEM_ERROR("J001", "배치 아이템 처리 중 오류가 발생했습니다"),
    BATCH_COMPENSATION_ERROR("J002", "배치 보상 처리 중 오류가 발생했습니다"),
    
    // === 데이터베이스 에러 (D000~D099) ===
    DATABASE_CONNECTION_ERROR("D000", "데이터베이스 연결 오류입니다"),
    DATABASE_QUERY_ERROR("D001", "데이터베이스 쿼리 실행 오류입니다"),
    DATABASE_CONSTRAINT_VIOLATION("D002", "데이터베이스 제약 조건 위반입니다"),
    
    // === 외부 서비스 에러 (S000~S099) ===
    EXTERNAL_SERVICE_ERROR("S000", "외부 서비스 호출 오류입니다"),
    EXTERNAL_SERVICE_TIMEOUT("S001", "외부 서비스 요청 시간 초과입니다"),
    EXTERNAL_SERVICE_UNAVAILABLE("S002", "외부 서비스를 이용할 수 없습니다");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 동적 메시지를 위한 포맷팅 (예: "잔고 부족: %s")
     */
    public String formatMessage(Object... args) {
        return String.format(this.message, args);
    }
    
    /**
     * 코드로 ErrorCode 찾기
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return COMMON_SYSTEM_ERROR; // 기본값
    }
}