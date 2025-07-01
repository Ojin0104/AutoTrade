package yj.AutoTrade;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import yj.AutoTrade.mcp.CoinPriceService;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class AutoTradeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoTradeApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider coinTools(CoinPriceService coinPriceService) {
		return MethodToolCallbackProvider.builder().toolObjects(coinPriceService).build();
	}

}
