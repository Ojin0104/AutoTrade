package yj.AutoTrade.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("SUCCESS", "요청 성공", null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청 성공", data);
    }
}
