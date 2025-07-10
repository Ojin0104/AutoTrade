package yj.AutoTrade.autotrade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yj.AutoTrade.autotrade.entity.AutoTradeConfig;
import yj.AutoTrade.autotrade.service.AutoTradeConfigService;
import yj.AutoTrade.common.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/autotrade")
@Tag(name = "AutoTrade Config API", description = "자동매매 설정 관리 API")
public class AutoTradeConfigController {

    private final AutoTradeConfigService autoTradeConfigService;

    @GetMapping
    @Operation(summary = "자동매매 설정 목록 조회")
    public ResponseEntity<ApiResponse<List<AutoTradeConfig>>> getAllConfigs() {
        List<AutoTradeConfig> configs = autoTradeConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/active")
    @Operation(summary = "활성화된 자동매매 설정 조회")
    public ResponseEntity<ApiResponse<List<AutoTradeConfig>>> getActiveConfigs() {
        List<AutoTradeConfig> configs = autoTradeConfigService.getActiveConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @PostMapping
    @Operation(summary = "자동매매 설정 생성")
    public ResponseEntity<ApiResponse<AutoTradeConfig>> createConfig(@RequestBody AutoTradeConfig config) {
        AutoTradeConfig savedConfig = autoTradeConfigService.createConfig(config);
        return ResponseEntity.ok(ApiResponse.success(savedConfig));
    }

    @PutMapping("/{id}")
    @Operation(summary = "자동매매 설정 수정")
    public ResponseEntity<ApiResponse<AutoTradeConfig>> updateConfig(@PathVariable Long id, @RequestBody AutoTradeConfig config) {
        AutoTradeConfig updatedConfig = autoTradeConfigService.updateConfig(id, config);
        return ResponseEntity.ok(ApiResponse.success(updatedConfig));
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "자동매매 활성화/비활성화")
    public ResponseEntity<ApiResponse<Void>> toggleConfig(@PathVariable Long id) {
        autoTradeConfigService.toggleConfig(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "자동매매 설정 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        autoTradeConfigService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/reset-daily-counters")
    @Operation(summary = "일일 카운터 리셋")
    public ResponseEntity<ApiResponse<Void>> resetDailyCounters() {
        autoTradeConfigService.resetDailyCounters();
        return ResponseEntity.ok(ApiResponse.success());
    }
}