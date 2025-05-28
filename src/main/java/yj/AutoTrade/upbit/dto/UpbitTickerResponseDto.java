package yj.AutoTrade.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpbitTickerResponseDto {

    private String market; // 종목 구분 코드

    @JsonProperty("trade_date")
    private String tradeDate; // 최근 거래 일자 (UTC, yyyyMMdd)

    @JsonProperty("trade_time")
    private String tradeTime; // 최근 거래 시각 (UTC, HHmmss)

    @JsonProperty("trade_date_kst")
    private String tradeDateKst; // 최근 거래 일자 (KST, yyyyMMdd)

    @JsonProperty("trade_time_kst")
    private String tradeTimeKst; // 최근 거래 시각 (KST, HHmmss)

    @JsonProperty("trade_timestamp")
    private Long tradeTimestamp; // 최근 거래 일시 (Unix Timestamp)

    @JsonProperty("opening_price")
    private Double openingPrice; // 시가

    @JsonProperty("high_price")
    private Double highPrice; // 고가

    @JsonProperty("low_price")
    private Double lowPrice; // 저가

    @JsonProperty("trade_price")
    private Double tradePrice; // 종가 (현재가)

    @JsonProperty("prev_closing_price")
    private Double prevClosingPrice; // 전일 종가 (UTC 0시 기준)

    private String change; // EVEN(보합), RISE(상승), FALL(하락)

    @JsonProperty("change_price")
    private Double changePrice; // 변화액의 절대값

    @JsonProperty("change_rate")
    private Double changeRate; // 변화율의 절대값

    @JsonProperty("signed_change_price")
    private Double signedChangePrice; // 부호가 있는 변화액

    @JsonProperty("signed_change_rate")
    private Double signedChangeRate; // 부호가 있는 변화율

    @JsonProperty("trade_volume")
    private Double tradeVolume; // 가장 최근 거래량

    @JsonProperty("acc_trade_price")
    private Double accTradePrice; // 누적 거래대금 (UTC 0시 기준)

    @JsonProperty("acc_trade_price_24h")
    private Double accTradePrice24h; // 24시간 누적 거래대금

    @JsonProperty("acc_trade_volume")
    private Double accTradeVolume; // 누적 거래량 (UTC 0시 기준)

    @JsonProperty("acc_trade_volume_24h")
    private Double accTradeVolume24h; // 24시간 누적 거래량

    @JsonProperty("highest_52_week_price")
    private Double highest52WeekPrice; // 52주 신고가

    @JsonProperty("highest_52_week_date")
    private String highest52WeekDate; // 52주 신고가 달성일 (yyyy-MM-dd)

    @JsonProperty("lowest_52_week_price")
    private Double lowest52WeekPrice; // 52주 신저가

    @JsonProperty("lowest_52_week_date")
    private String lowest52WeekDate; // 52주 신저가 달성일 (yyyy-MM-dd)

    private Long timestamp; // 타임스탬프
}
