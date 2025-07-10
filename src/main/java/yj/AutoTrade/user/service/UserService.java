package yj.AutoTrade.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yj.AutoTrade.auth.dto.SignupRequestDto;
import yj.AutoTrade.user.entity.User;
import yj.AutoTrade.user.entity.UserRole;
import yj.AutoTrade.user.entity.UserStatus;
import yj.AutoTrade.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        log.debug("로그인 시도: {}", email);
        return user;
    }

    public User createUser(SignupRequestDto signupRequest) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다: " + signupRequest.getEmail());
        }

        // 사용자명 중복 확인
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("이미 사용 중인 사용자명입니다: " + signupRequest.getUsername());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // 사용자 생성
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(encodedPassword)
                .name(signupRequest.getName())
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("새 사용자 생성: {}, 이메일: {}", savedUser.getUsername(), savedUser.getEmail());
        
        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
    }

    public User findByIdWithApiKeys(Long id) {
        return userRepository.findByIdWithApiKeys(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
    }

    public void updateLastLogin(String email) {
        User user = findByEmail(email);
        user.updateLastLogin();
        userRepository.save(user);
        log.debug("마지막 로그인 시간 업데이트: {}", email);
    }

    public void activateUser(Long userId) {
        User user = findById(userId);
        user.activate();
        userRepository.save(user);
        log.info("사용자 활성화: {}", user.getEmail());
    }

    public void deactivateUser(Long userId) {
        User user = findById(userId);
        user.deactivate();
        userRepository.save(user);
        log.info("사용자 비활성화: {}", user.getEmail());
    }

    public void suspendUser(Long userId) {
        User user = findById(userId);
        user.suspend();
        userRepository.save(user);
        log.info("사용자 정지: {}", user.getEmail());
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
}