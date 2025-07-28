package yj.AutoTrade.api.upbit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpbitErrorResponse {
    private UpbitError error;

    @Getter
    @NoArgsConstructor
    public static class UpbitError {
        private String name;      // 에러 코드
        private String message;   // 에러 메시지
    }
}
