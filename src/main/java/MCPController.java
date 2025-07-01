import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/tools")
public class MCPController {

    @PostMapping("/getCoinPrice")
    public ResponseEntity<Map<String, Object>> getCoinPrice(@RequestBody Map<String, String> body) {
        String coin = body.getOrDefault("coin", "BTC");

        Map<String, Object> output = Map.of(
                "type", "tool_use",
                "output", Map.of(
                        "coin", coin,
                        "price", 71000000
                )
        );

        return ResponseEntity.ok(output);
    }
}
