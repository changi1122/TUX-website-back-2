package kr.ac.cbnu.tux.global.scheduler;

import kr.ac.cbnu.tux.domain.common.service.CleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final CleanupService cleanupService;

    // 매월 1일 새벽 1시에 실행
    @Scheduled(cron = "0 0 1 1 * *")
    public void cleanupDeletedCommunityPosts() {
        cleanupService.deleteExpiredDeletedPosts();
    }

    // 매월 1일 새벽 2시에 실행
    @Scheduled(cron = "0 0 2 1 * *")
    public void cleanupDeletedReferenceRoomData() {
        cleanupService.deleteExpiredDeletedData();
    }

    // 매월 1일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 1 * *")
    public void cleanupUnusedFiles() {
        cleanupService.deleteUnusedFiles();
    }

    // 매일 1일 새벽 4시에 실행
    @Scheduled(cron = "0 0 5 * * *")
    public void deleteExpiredRefreshTokens() {
        cleanupService.deleteExpiredRefreshTokens();
    }
}
