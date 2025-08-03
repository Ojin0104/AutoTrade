package yj.AutoTrade.api.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateRowDto {
    
    @JsonProperty("UNIT_NM")
    private String unitName;

    @JsonProperty("DATA_VALUE")
    private String dataValue;

    @JsonProperty("TIME")
    private String time;
}