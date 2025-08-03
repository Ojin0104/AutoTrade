package yj.AutoTrade.autotrade.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoTradeScheduler {

    private final JobLauncher jobLauncher;
    private final Job autoTradeJob;

    @Scheduled(cron = "0 */10 * * * ?") // 10분마다 실행
    public void runAutoTrade() {
        try {
            log.info("자동매매 배치 작업 시작");
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(autoTradeJob, jobParameters);
            log.info("자동매매 배치 작업 완료");
            
        } catch (Exception e) {
            log.error("자동매매 배치 작업 실패: {}", e.getMessage(), e);
        }
    }
}