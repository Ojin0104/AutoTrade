package yj.AutoTrade.kimp.entity;

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
@Table(name = "kimp_history", 
       indexes = {
           @Index(name = "idx_symbol_datetime", columnList = "upbit_symbol, binance_symbol, collected_at"),
           @Index(name = "idx_collected_at", columnList = "collected_at")
       })
public class KimpHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String upbitSymbol;

    @Column(nullable = false, length = 20)
    private String binanceSymbol;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal upbitPrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal binancePrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal binancePriceKrw;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal usdtKrwRate;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal kimpRate;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal priceDifference;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal upbitVolume24h;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal binanceVolume24h;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    @Column(length = 500)
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CollectionStatus status = CollectionStatus.SUCCESS;

    // 김프 방향성 계산
    public boolean isPositiveKimp() {
        return kimpRate.compareTo(BigDecimal.ZERO) > 0;
    }

    // 김프 절댓값
    public BigDecimal getAbsoluteKimpRate() {
        return kimpRate.abs();
    }

    // 수익성 여부 (임계값 이상)
    public boolean isProfitable(BigDecimal threshold) {
        return getAbsoluteKimpRate().compareTo(threshold) >= 0;
    }
}