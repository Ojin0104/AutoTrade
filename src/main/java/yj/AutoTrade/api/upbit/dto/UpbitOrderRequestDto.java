package yj.AutoTrade.api.upbit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 값 필드는 JSON에서 제외
public class UpbitOrderRequestDto extends UpbitRequestParamsDto {


        private String market; // 마켓 ID (필수)

        private String side; // 주문 종류 (bid: 매수, ask: 매도)

        @JsonProperty("volume")
        private BigDecimal volume; // 주문량 (지정가, 시장가 매도 시 필수, NumberString)

        @JsonProperty("price")
        private BigDecimal price; // 주문 가격 (지정가, 시장가 매수 시 필수, NumberString)

        @JsonProperty("ord_type")
        private UpbitOrderType ordType; // 주문 타입 (limit, price, market, best)

        private String identifier; // 조회용 사용자 지정값 (선택, Unique 값 사용)

        @JsonProperty("time_in_force")
        private String timeInForce; // IOC, FOK 주문 설정 (best, limit에서만 사용 가능)


}
