package yj.AutoTrade.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yj.AutoTrade.user.entity.User;
import yj.AutoTrade.user.entity.UserStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByStatus(UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.role = 'USER'")
    List<User> findActiveUsers(@Param("status") UserStatus status);
    
    @Query("SELECT u FROM User u JOIN FETCH u.apiKeys WHERE u.id = :id")
    Optional<User> findByIdWithApiKeys(@Param("id") Long id);
}