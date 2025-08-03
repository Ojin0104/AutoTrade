package yj.AutoTrade.kimp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yj.AutoTrade.common.ApiResponse;
import yj.AutoTrade.kimp.entity.CoinPair;
import yj.AutoTrade.kimp.service.CoinPairService;

import java.util.List;

@RestController
@RequestMapping("/api/coin-pairs")
@RequiredArgsConstructor
@Tag(name = "CoinPair API", description = "코인 페어 관리 API")
public class CoinPairController {

    private final CoinPairService coinPairService;

    @GetMapping
    @Operation(summary = "모든 코인 페어 조회")
    public ResponseEntity<ApiResponse<List<CoinPair>>> getAllCoinPairs() {
        List<CoinPair> pairs = coinPairService.getAllCoinPairs();
        return ResponseEntity.ok(ApiResponse.success(pairs));
    }

    @GetMapping("/active")
    @Operation(summary = "활성 코인 페어 조회")
    public ResponseEntity<ApiResponse<List<CoinPair>>> getActiveCoinPairs() {
        List<CoinPair> pairs = coinPairService.getActiveCoinPairs();
        return ResponseEntity.ok(ApiResponse.success(pairs));
    }

    @GetMapping("/batch-enabled")
    @Operation(summary = "배치 수집 대상 코인 페어 조회")
    public ResponseEntity<ApiResponse<List<CoinPair>>> getBatchEnabledCoinPairs() {
        List<CoinPair> pairs = coinPairService.getBatchEnabledCoinPairs();
        return ResponseEntity.ok(ApiResponse.success(pairs));
    }

    @PostMapping
    @Operation(summary = "코인 페어 생성")
    public ResponseEntity<ApiResponse<CoinPair>> createCoinPair(@RequestBody CoinPair coinPair) {
        CoinPair created = coinPairService.createCoinPair(coinPair);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "코인 페어 수정")
    public ResponseEntity<ApiResponse<CoinPair>> updateCoinPair(@PathVariable Long id, @RequestBody CoinPair coinPair) {
        CoinPair updated = coinPairService.updateCoinPair(id, coinPair);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "코인 페어 활성화/비활성화")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id) {
        coinPairService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}/toggle-batch")
    @Operation(summary = "배치 수집 활성화/비활성화")
    public ResponseEntity<ApiResponse<Void>> toggleBatch(@PathVariable Long id) {
        coinPairService.toggleBatch(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}/enable-batch")
    @Operation(summary = "배치 수집 활성화")
    public ResponseEntity<ApiResponse<Void>> enableBatch(@PathVariable Long id) {
        coinPairService.enableBatch(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}/disable-batch")
    @Operation(summary = "배치 수집 비활성화")
    public ResponseEntity<ApiResponse<Void>> disableBatch(@PathVariable Long id) {
        coinPairService.disableBatch(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "코인 페어 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteCoinPair(@PathVariable Long id) {
        coinPairService.deleteCoinPair(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}