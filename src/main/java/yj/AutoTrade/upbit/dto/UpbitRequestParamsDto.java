package yj.AutoTrade.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class UpbitRequestParamsDto {
    public Map<String, String> toHashMap() {
        Map<String, String> map = new HashMap<>();

        // DTO의 모든 필드에 대해 반복
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true); // 필드 접근 허용

            try {
                Object value = field.get(this); // 필드 값 가져오기
                if (value != null) { // null 값 제외
                    String key = field.isAnnotationPresent(JsonProperty.class)
                            ? field.getAnnotation(JsonProperty.class).value() // @JsonProperty 값 사용
                            : field.getName(); // 필드명 사용
                    map.put(key, value.toString()); // HashMap에 추가
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
