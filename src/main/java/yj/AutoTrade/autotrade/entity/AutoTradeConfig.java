package yj.AutoTrade.autotrade.entity;

import jakarta.persistence.*;
import lombok.*;
import yj.AutoTrade.common.BaseTimeEntity;
import yj.AutoTrade.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auto_trade_config")
public class AutoTradeConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String upbitSymbol;

    @Column(nullable = false, length = 20)
    private String binanceSymbol;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal seedAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal kimpThreshold;

    @Column(nullable = false)
    private BigDecimal leverage;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Integer maxDailyTrades;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentDailyTrades = 0;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxDailyLoss;

    @Column(nullable = false, precision = 20, scale = 8)
    @Builder.Default
    private BigDecimal currentDailyLoss = BigDecimal.ZERO;

    private LocalDateTime lastExecutedAt;

    @Column(length = 100)
    private String description;

    public void incrementDailyTrades() {
        this.currentDailyTrades = this.currentDailyTrades + 1;
    }

    public void addDailyLoss(BigDecimal loss) {
        this.currentDailyLoss = this.currentDailyLoss.add(loss);
    }

    public void resetDailyCounters() {
        this.currentDailyTrades = 0;
        this.currentDailyLoss = BigDecimal.ZERO;
    }

    public boolean canExecuteTrade() {
        return isActive && 
               user.canTrade() &&
               currentDailyTrades < maxDailyTrades && 
               currentDailyLoss.compareTo(maxDailyLoss) < 0;
    }
}