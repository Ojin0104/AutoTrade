package yj.AutoTrade.user.entity;

import jakarta.persistence.*;
import lombok.*;
import yj.AutoTrade.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "api_keys")
public class ApiKey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeType exchange;

    @Column(nullable = false, length = 500)
    private String accessKey; // 암호화된 액세스 키

    @Column(nullable = false, length = 500)
    private String secretKey; // 암호화된 시크릿 키

    @Column(length = 500)
    private String passphrase; // 암호화된 패스프레이즈 (바이낸스 등에서 사용)

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(length = 100)
    private String alias; // 사용자 정의 별명

    private LocalDateTime lastUsedAt;

    private LocalDateTime expiresAt; // API 키 만료일

    @Column(length = 500)
    private String description; // API 키 설명

    // 비즈니스 메소드
    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isUsable() {
        return isActive && !isExpired();
    }

    public boolean isUpbitKey() {
        return exchange == ExchangeType.UPBIT;
    }

    public boolean isBinanceKey() {
        return exchange == ExchangeType.BINANCE;
    }
}