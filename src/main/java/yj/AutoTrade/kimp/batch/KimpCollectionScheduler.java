package yj.AutoTrade.kimp.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yj.AutoTrade.kimp.service.KimpCollectionService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KimpCollectionScheduler {

    private final KimpCollectionService kimpCollectionService;

    @Scheduled(cron = "0 */10 * * * ?") // 10분마다 실행
    public void collectKimpData() {
        try {
            log.info("=== 김프 데이터 수집 스케줄러 시작 ===");
            kimpCollectionService.collectAllKimpData();
            log.info("=== 김프 데이터 수집 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("김프 데이터 수집 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    // 테스트용 수동 실행 (1분마다)
    //@Scheduled(cron = "0 */1 * * * ?")
    public void collectKimpDataForTest() {
        try {
            log.info("=== 테스트용 김프 데이터 수집 시작 ===");
            kimpCollectionService.collectAllKimpData();
            log.info("=== 테스트용 김프 데이터 수집 완료 ===");
        } catch (Exception e) {
            log.error("테스트용 김프 데이터 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}