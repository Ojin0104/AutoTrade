package yj.AutoTrade.api.binance.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@ToString
@Getter
public class BinanceOrderRequestDto {

    @NotBlank(message = "symbol은 필수입니다.")
    private String symbol;

    @NotNull(message = "side는 필수입니다.")
    private OrderSide side;

    @NotNull(message = "type은 필수입니다.")
    private OrderType type;

    private TimeInForce timeInForce;

    @DecimalMin(value = "0.00000001", inclusive = true, message = "quantity는 0보다 커야 합니다.")
    private BigDecimal quantity;

    @DecimalMin(value = "0.00000001", inclusive = true, message = "quoteOrderQty는 0보다 커야 합니다.")
    private BigDecimal quoteOrderQty;

    @DecimalMin(value = "0.00000001", inclusive = true, message = "price는 0보다 커야 합니다.")
    private BigDecimal price;

    private String newClientOrderId;

    private Long strategyId;

    @Min(value = 1000000, message = "strategyType은 1000000 이상이어야 합니다.")
    private Integer strategyType;

    @DecimalMin(value = "0.00000001", inclusive = true, message = "stopPrice는 0보다 커야 합니다.")
    private BigDecimal stopPrice;

    @Min(value = 1, message = "trailingDelta는 1 이상이어야 합니다.")
    private Long trailingDelta;

    @DecimalMin(value = "0.00000001", inclusive = true, message = "icebergQty는 0보다 커야 합니다.")
    private BigDecimal icebergQty;

    private NewOrderRespType newOrderRespType;

    private SelfTradePreventionMode selfTradePreventionMode;

    @Max(value = 60000, message = "recvWindow는 60000 이하여야 합니다.")
    private Long recvWindow;

    @NotNull(message = "timestamp는 필수입니다.")
    private Long timestamp;
}
