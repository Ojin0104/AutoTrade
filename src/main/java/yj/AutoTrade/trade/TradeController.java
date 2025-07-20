package yj.AutoTrade.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yj.AutoTrade.common.ApiResponse;
import yj.AutoTrade.trade.dto.TradeRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/trade")
@Tag(name = "Trade API", description = "거래 관련 API")
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/order")
    @Operation(summary = "주문 생성", description = "업비트 및 바이낸스 거래를 처리합니다.")
    public ResponseEntity createOrder(@RequestBody TradeRequestDto tradeRequestDto){
        tradeService.trade(tradeRequestDto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/close")
    @Operation(summary = "포지션 정리", description = "업비트 매도 및 바이낸스 숏 포지션을 정리합니다.")
    public ResponseEntity closeTrade(@RequestBody TradeRequestDto tradeRequestDto){
        tradeService.closeTrade(tradeRequestDto);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
