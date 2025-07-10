package yj.AutoTrade.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import yj.AutoTrade.auth.dto.JwtResponseDto;
import yj.AutoTrade.auth.dto.LoginRequestDto;
import yj.AutoTrade.auth.dto.SignupRequestDto;
import yj.AutoTrade.auth.service.JwtTokenService;
import yj.AutoTrade.common.ApiResponse;
import yj.AutoTrade.user.entity.User;
import yj.AutoTrade.user.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "인증 관련 API")
public class AuthController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        try {
            userService.createUser(signupRequest);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("SIGNUP_FAILED", e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    public ResponseEntity<ApiResponse<JwtResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            // 인증 수행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 인증된 사용자 정보 가져오기
            User user = (User) authentication.getPrincipal();
            
            // JWT 토큰 생성
            String jwt = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
            
            // 마지막 로그인 시간 업데이트
            userService.updateLastLogin(user.getEmail());
            
            // 응답 생성
            JwtResponseDto response = new JwtResponseDto(
                    jwt,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            );
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("LOGIN_FAILED", "이메일 또는 비밀번호가 올바르지 않습니다.", null));
        }
    }

    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 확인", description = "이메일이 사용 가능한지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.success(isAvailable));
    }

    @GetMapping("/check-username")
    @Operation(summary = "사용자명 중복 확인", description = "사용자명이 사용 가능한지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(ApiResponse.success(isAvailable));
    }
}