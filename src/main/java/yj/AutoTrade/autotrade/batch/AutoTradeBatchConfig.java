package yj.AutoTrade.autotrade.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import yj.AutoTrade.autotrade.entity.AutoTradeConfig;
import yj.AutoTrade.autotrade.repository.AutoTradeConfigRepository;

import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class AutoTradeBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AutoTradeConfigRepository autoTradeConfigRepository;
    private final AutoTradeProcessor autoTradeProcessor;
    private final AutoTradeWriter autoTradeWriter;

    @Bean
    public Job autoTradeJob() {
        return new JobBuilder("autoTradeJob", jobRepository)
                .start(autoTradeStep())
                .build();
    }

    @Bean
    public Step autoTradeStep() {
        return new StepBuilder("autoTradeStep", jobRepository)
                .<AutoTradeConfig, AutoTradeConfig>chunk(10, transactionManager)
                .reader(autoTradeReader())
                .processor(autoTradeProcessor)
                .writer(autoTradeWriter)
                .build();
    }

    @Bean
    public RepositoryItemReader<AutoTradeConfig> autoTradeReader() {
        return new RepositoryItemReaderBuilder<AutoTradeConfig>()
                .name("autoTradeReader")
                .repository(autoTradeConfigRepository)
                .methodName("findExecutableConfigs")
                .pageSize(10)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }
}