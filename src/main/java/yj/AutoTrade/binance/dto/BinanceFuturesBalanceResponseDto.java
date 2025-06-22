package yj.AutoTrade.binance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BinanceFuturesBalanceResponseDto {
    @JsonProperty("accountAlias")
    private String accountAlias;
    
    @JsonProperty("asset")
    private String asset;
    
    @JsonProperty("balance")
    private String balance;
    
    @JsonProperty("crossWalletBalance")
    private String crossWalletBalance;
    
    @JsonProperty("crossUnPnl")
    private String crossUnPnl;
    
    @JsonProperty("availableBalance")
    private String availableBalance;
    
    @JsonProperty("maxWithdrawAmount")
    private String maxWithdrawAmount;
    
    @JsonProperty("marginAvailable")
    private boolean marginAvailable;
    
    @JsonProperty("updateTime")
    private long updateTime;
} 