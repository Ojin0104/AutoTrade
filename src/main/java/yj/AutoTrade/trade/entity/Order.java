package yj.AutoTrade.trade.entity;

import jakarta.persistence.*;
import lombok.*;
import yj.AutoTrade.common.BaseTimeEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders") // "order"는 SQL 예약어이므로 "orders"로 테이블명 지정
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Upbit 주문 UUID
    @Column(unique = true)
    private String upbitOrderId;

    // Binance 주문 ID
    @Column(unique = true)
    private String binanceOrderId;

    // Upbit 심볼 (e.g., "KRW-BTC")
    @Column(nullable = false, length = 20)
    private String upbitSymbol;

    // Binance 심볼 (e.g., "BTCUSDT")
    @Column(nullable = false, length = 20)
    private String binanceSymbol;

    // 포지션 상태: OPEN, CLOSED, FAILED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // 주문 수량 (코인 기준)
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    // Upbit 평균 체결가 (KRW)
    @Column(precision = 20, scale = 8)
    private BigDecimal upbitAvgPrice;

    // Binance 평균 체결가 (USDT)
    @Column(precision = 20, scale = 8)
    private BigDecimal binanceAvgPrice;
    
    // Binance 레버리지
    @Column(nullable = false)
    private BigDecimal leverage;
    
    // 실현 손익
    @Column(precision = 20, scale = 8)
    private BigDecimal profit;

    // 포지션 종료 시각
    private LocalDateTime closedAt;
}
