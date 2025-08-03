package yj.AutoTrade.kimp.entity;

public enum CollectionStatus {
    SUCCESS,        // 수집 성공
    UPBIT_ERROR,    // 업비트 API 오류
    BINANCE_ERROR,  // 바이낸스 API 오류
    CALCULATION_ERROR, // 김프 계산 오류
    NETWORK_ERROR   // 네트워크 오류
}