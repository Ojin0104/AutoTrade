package yj.AutoTrade.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpbitAccountResponseDto {
    @JsonProperty("currency")
    private String currency;

    @JsonProperty("balance")
    private String balance;

    @JsonProperty("locked")
    private String locked;

    @JsonProperty("avg_buy_price")
    private String avgBuyPrice;

    @JsonProperty("avg_buy_price_modified")
    private boolean avgBuyPriceModified;

    @JsonProperty("unit_currency")
    private String unitCurrency;
}
