package yj.AutoTrade.binance.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
public class BinanceFuturesOrderRequestDto {

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{6,20}$", message = "symbol must be uppercase and alphanumeric")
    private String symbol;

    @NotNull
    private OrderSide side;

    @NotNull
    private OrderType type;

    @NotNull
    private Long timestamp;


    // ✅ 조건부 필수 or 선택 필드
    // 선택 or 조건부 필수
    private PositionSide positionSide;

    private TimeInForce timeInForce;

    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "quantity must be a valid decimal string")
    private String quantity;

    @Pattern(regexp = "true|false", message = "reduceOnly must be true or false")
    private String reduceOnly;

    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "price must be a valid decimal string")
    private String price;

    @Size(max = 36, message = "newClientOrderId must be 1~36 characters")
    @Pattern(regexp = "^[\\.A-Z\\:/a-z0-9_-]{1,36}$", message = "invalid client order ID format")
    private String newClientOrderId;

    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "stopPrice must be a valid decimal string")
    private String stopPrice;

    @Pattern(regexp = "true|false", message = "closePosition must be true or false")
    private String closePosition;

    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "activationPrice must be a valid decimal string")
    private String activationPrice;

    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "callbackRate must be a valid decimal string between 0.1 and 10")
    private String callbackRate;

    @Pattern(regexp = "MARK_PRICE|CONTRACT_PRICE", message = "workingType must be MARK_PRICE or CONTRACT_PRICE")
    private String workingType;

    @Pattern(regexp = "true|false", message = "priceProtect must be true or false")
    private String priceProtect;

    @Pattern(regexp = "ACK|RESULT", message = "newOrderRespType must be ACK or RESULT")
    private NewOrderRespType newOrderRespType;

    @Pattern(regexp = "OPPONENT(_5|_10|_20)?|QUEUE(_5|_10|_20)?|NONE", message = "invalid priceMatch mode")
    private String priceMatch;

    @Pattern(regexp = "EXPIRE_TAKER|EXPIRE_MAKER|EXPIRE_BOTH|NONE", message = "invalid selfTradePreventionMode")
    private String selfTradePreventionMode;

    private Long goodTillDate;

    private Long recvWindow;

    @NotBlank(message = "signature is required")
    private String signature;
}
