package yj.AutoTrade.user.entity;

public enum ExchangeType {
    UPBIT("업비트"),
    BINANCE("바이낸스"),
    BINANCE_FUTURES("바이낸스 선물");

    private final String description;

    ExchangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}