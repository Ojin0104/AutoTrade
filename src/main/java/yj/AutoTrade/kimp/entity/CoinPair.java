package yj.AutoTrade.kimp.entity;

import jakarta.persistence.*;
import lombok.*;
import yj.AutoTrade.common.BaseTimeEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "coin_pairs",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"upbit_symbol", "binance_symbol"})
       })
public class CoinPair extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String upbitSymbol;

    @Column(nullable = false, length = 20)
    private String binanceSymbol;

    @Column(nullable = false, length = 10)
    private String coinName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, length = 1)
    @Builder.Default
    private String batchEnabled = "N";

    @Column(length = 100)
    private String description;

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void enableBatch() {
        this.batchEnabled = "Y";
    }

    public void disableBatch() {
        this.batchEnabled = "N";
    }

    public boolean isBatchEnabled() {
        return "Y".equals(this.batchEnabled);
    }
}