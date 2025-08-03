package yj.AutoTrade.api.binance.dto;

import lombok.Data;

@Data
public class BinanceFuturesOrderResponseDto {
    private String clientOrderId;
    private String cumQty;
    private String cumQuote;
    private String executedQty;
    private Long orderId;
    private String avgPrice;
    private String origQty;
    private String price;
    private Boolean reduceOnly;
    private String side;
    private String positionSide;
    private String status;
    private String stopPrice;
    private Boolean closePosition;
    private String symbol;
    private String timeInForce;
    private String type;
    private String origType;
    private String activatePrice;
    private String priceRate;
    private Long updateTime;
    private String workingType;
    private Boolean priceProtect;
    private String priceMatch;
    private String selfTradePreventionMode;
    private Long goodTillDate;
}
