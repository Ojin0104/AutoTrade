package yj.AutoTrade.api.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticSearchDto {
    
    @JsonProperty("list_total_count")
    private int listTotalCount;

    @JsonProperty("RESULT")
    private ResultDto result;

    @JsonProperty("row")
    private List<ExchangeRateRowDto> rows;
}