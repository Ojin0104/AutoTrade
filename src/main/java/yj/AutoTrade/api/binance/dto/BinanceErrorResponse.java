package yj.AutoTrade.api.binance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BinanceErrorResponse {
    private int code;       // 예: -2010
    private String msg;     // 예: "Account has insufficient balance for requested action."
}
