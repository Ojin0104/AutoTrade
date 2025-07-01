package yj.AutoTrade.trade.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import yj.AutoTrade.common.BaseTimeEntity;
import java.math.BigDecimal;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "trade_compensation_queue")
public class TradeCompensationQueue extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 업비트 매수 주문 정보
    @Column(nullable = false)
    private String upbitOrderId;

    // 거래한 코인 예: BTC, ETH
    @Column(nullable = false, length = 20)
    private String symbol;

    // 거래 수량
    @Column(nullable = false)
    private BigDecimal quantity;

    // 거래 가격 (선택적)
    private BigDecimal price;

    // 상태: PENDING, SUCCESS, FAILED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TradeCompensationStatus status;

    // 에러 메시지 (보상 실패 시 기록)
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // 재시도 횟수
    @Column(nullable = false)
    private int retryCount = 0;

    // 마지막 재시도 시각
    private LocalDateTime lastAttemptAt;
}

