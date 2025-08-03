package yj.AutoTrade.api.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponseDto {

    @JsonProperty("StatisticSearch")
    private StatisticSearchDto statisticSearch;
}