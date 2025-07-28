package yj.AutoTrade.api.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultDto {
    
    @JsonProperty("RESULT_CODE")
    private String resultCode;

    @JsonProperty("RESULT_MESSAGE")
    private String resultMessage;
}