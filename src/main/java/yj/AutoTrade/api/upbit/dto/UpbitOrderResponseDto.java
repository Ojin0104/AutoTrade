package yj.AutoTrade.api.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.math.BigDecimal;
@Getter
public class UpbitOrderResponseDto {


        private String uuid; // 주문의 고유 아이디
        private String side; // 주문 종류
        private String ordType; // 주문 방식
        private BigDecimal price; // 주문 당시 화폐 가격 (NumberString)
        private String state; // 주문 상태
        private String market; // 마켓의 유일키
        private String createdAt; // 주문 생성 시간
        private BigDecimal volume; // 사용자가 입력한 주문 양 (NumberString)

        @JsonProperty("remaining_volume")
        private String remainingVolume; // 체결 후 남은 주문 양 (NumberString)

        @JsonProperty("reserved_fee")
        private String reservedFee; // 수수료로 예약된 비용 (NumberString)

        @JsonProperty("remaining_fee")
        private String remainingFee; // 남은 수수료 (NumberString)

        @JsonProperty("paid_fee")
        private String paidFee; // 사용된 수수료 (NumberString)

        private String locked; // 거래에 사용중인 비용 (NumberString)

        @JsonProperty("executed_volume")
        private String executedVolume; // 체결된 양 (NumberString)

        @JsonProperty("trades_count")
        private Integer tradesCount; // 해당 주문에 걸린 체결 수 (Integer)

        @JsonProperty("time_in_force")
        private String timeInForce; // IOC, FOK 설정

        private String identifier; // 조회용 사용자 지정값 (2024-10-18 이후 주문만 제공)


}
