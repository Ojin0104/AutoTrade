package yj.AutoTrade.trade.entity;

public enum OrderStatus {
    OPEN,      // 포지션 진입 성공
    CLOSED,    // 포지션 청산 완료
    FAILED     // 포지션 진입/청산 실패
} 