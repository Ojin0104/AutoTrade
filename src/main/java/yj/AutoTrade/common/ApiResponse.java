package yj.AutoTrade.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse {
    private String code;
    private String message;


    public static ApiResponse success() {
        return new ApiResponse("SUCCESS", "요청 성공");
    }


}
