package yj.AutoTrade.trade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yj.AutoTrade.trade.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
} 