package yj.AutoTrade.kimp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yj.AutoTrade.common.ApiResponse;
import yj.AutoTrade.kimp.dto.KimpDataDto;
import yj.AutoTrade.kimp.dto.KimpStatisticsDto;
import yj.AutoTrade.kimp.entity.CoinPair;
import yj.AutoTrade.kimp.entity.KimpHistory;
import yj.AutoTrade.kimp.service.KimpQueryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/kimp")
@RequiredArgsConstructor
@Tag(name = "Kimp API", description = "김프 데이터 조회 API")
public class KimpController {

    private final KimpQueryService kimpQueryService;

    @GetMapping("/latest")
    @Operation(summary = "최신 김프 데이터 조회", description = "모든 코인의 최신 김프 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<List<KimpDataDto>>> getLatestKimpData() {
        List<KimpDataDto> data = kimpQueryService.getLatestKimpForAllCoins();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/latest/{upbitSymbol}/{binanceSymbol}")
    @Operation(summary = "특정 코인 최신 김프", description = "특정 코인의 최신 김프 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<KimpDataDto>> getLatestKimpForCoin(
            @PathVariable String upbitSymbol,
            @PathVariable String binanceSymbol) {
        KimpDataDto data = kimpQueryService.getLatestKimpForCoin(upbitSymbol, binanceSymbol);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/history/{upbitSymbol}/{binanceSymbol}")
    @Operation(summary = "김프 히스토리 조회", description = "특정 기간의 김프 히스토리를 조회합니다.")
    public ResponseEntity<ApiResponse<List<KimpDataDto>>> getKimpHistory(
            @PathVariable String upbitSymbol,
            @PathVariable String binanceSymbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<KimpDataDto> data = kimpQueryService.getKimpHistory(upbitSymbol, binanceSymbol, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/recent/{upbitSymbol}/{binanceSymbol}")
    @Operation(summary = "최근 24시간 김프", description = "최근 24시간 김프 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<List<KimpDataDto>>> getRecentKimpData(
            @PathVariable String upbitSymbol,
            @PathVariable String binanceSymbol) {
        List<KimpDataDto> data = kimpQueryService.getRecentKimpData(upbitSymbol, binanceSymbol);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/profitable")
    @Operation(summary = "수익성 김프 조회", description = "임계값 이상의 김프 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<List<KimpDataDto>>> getProfitableKimp(
            @RequestParam String upbitSymbol,
            @RequestParam String binanceSymbol,
            @RequestParam BigDecimal minKimp,
            @RequestParam(defaultValue = "24") int hours) {
        List<KimpDataDto> data = kimpQueryService.getProfitableKimp(upbitSymbol, binanceSymbol, minKimp, hours);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/statistics/{upbitSymbol}/{binanceSymbol}")
    @Operation(summary = "김프 통계", description = "특정 기간의 김프 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<KimpStatisticsDto>> getKimpStatistics(
            @PathVariable String upbitSymbol,
            @PathVariable String binanceSymbol,
            @RequestParam(defaultValue = "24") int hours) {
        KimpStatisticsDto statistics = kimpQueryService.getKimpStatistics(upbitSymbol, binanceSymbol, hours);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/coin-pairs")
    @Operation(summary = "활성 코인 페어 조회", description = "김프 수집 대상 코인 페어 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CoinPair>>> getActiveCoinPairs() {
        List<CoinPair> pairs = kimpQueryService.getActiveCoinPairs();
        return ResponseEntity.ok(ApiResponse.success(pairs));
    }

    @PostMapping("/collect-now")
    @Operation(summary = "수동 김프 수집", description = "즉시 김프 데이터를 수집합니다.")
    public ResponseEntity<ApiResponse<Void>> collectKimpDataNow() {
        kimpQueryService.collectKimpDataManually();
        return ResponseEntity.ok(ApiResponse.success());
    }
}