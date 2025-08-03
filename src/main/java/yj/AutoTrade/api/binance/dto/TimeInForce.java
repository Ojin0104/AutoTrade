package yj.AutoTrade.api.binance.dto;

public enum TimeInForce {
    /**
     * Good Til Canceled - 유저가 직접 취소할 때까지 주문이 유효함
     */
    GTC,

    /**
     * Immediate Or Cancel - 가능한 부분만 즉시 체결하고 나머지는 취소
     */
    IOC,

    /**
     * Fill Or Kill - 전체 주문이 즉시 체결되지 않으면 전부 취소
     */
    FOK
}
