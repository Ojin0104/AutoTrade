package yj.AutoTrade.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yj.AutoTrade.user.entity.ApiKey;
import yj.AutoTrade.user.entity.ExchangeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    List<ApiKey> findByUserId(Long userId);
    
    List<ApiKey> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<ApiKey> findByUserIdAndExchange(Long userId, ExchangeType exchange);
    
    Optional<ApiKey> findByUserIdAndExchangeAndIsActiveTrue(Long userId, ExchangeType exchange);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.user.id = :userId AND ak.exchange = :exchange AND ak.isActive = true")
    Optional<ApiKey> findActiveApiKey(@Param("userId") Long userId, @Param("exchange") ExchangeType exchange);
    
    boolean existsByUserIdAndExchange(Long userId, ExchangeType exchange);
}