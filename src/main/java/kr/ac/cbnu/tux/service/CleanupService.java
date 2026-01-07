package kr.ac.cbnu.tux.service;

import kr.ac.cbnu.tux.domain.Attachment;
import kr.ac.cbnu.tux.repository.*;
import kr.ac.cbnu.tux.utility.FileStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private static final int BATCH_SIZE = 100;

    private final CommunityRepository communityRepository;
    private final ReferenceRoomRepository referenceRoomRepository;
    private final LikeRepository likeRepository;
    private final CmCommentRepository cmCommentRepository;
    private final RfCommentRepository rfCommentRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStore fileStore;
    private final TransactionTemplate transactionTemplate;

    public void deleteExpiredDeletedPosts() {
        LocalDateTime threshold = LocalDateTime.now().minusYears(1);
        int totalDeleted = 0;
        int maxLoops = 100; // 무한 루프 방지용

        log.info("[삭제 1년 경과 게시물 정리 시작] 기준 시간: {} 이전", threshold);

        for (int i = 0; i <= maxLoops; i++) {
            List<Long> postIds = communityRepository.findExpiredDeletedPostIds(threshold, BATCH_SIZE);
            if (postIds.isEmpty()) {
                break;
            }

            try {
                Integer deletedCounts = transactionTemplate.execute(status -> {
                    likeRepository.deleteByPostIds(postIds);
                    cmCommentRepository.deleteByPostIds(postIds);
                    attachmentRepository.deleteByPostIds(postIds);
                    return communityRepository.deleteByIds(postIds);
                });

                if (deletedCounts == null || deletedCounts == 0) {
                    break;
                }

                totalDeleted += deletedCounts;
                log.info("[Batch {}] {}개 삭제 완료 (누적: {})", i + 1, deletedCounts, totalDeleted);
            }
            catch (Exception e) {
                log.error("[삭제 처리 중 오류 발생] postIds={}. 프로세스를 중단합니다.", postIds, e);
                break;
            }
        }

        log.info("[삭제 1년 경과 게시물 정리 종료] 총 {}개 삭제됨", totalDeleted);
    }

    public void deleteExpiredDeletedData() {
        LocalDateTime threshold = LocalDateTime.now().minusYears(1);
        int totalDeleted = 0;
        int maxLoops = 100; // 무한 루프 방지용

        log.info("[삭제 1년 경과 자료실 정리 시작] 기준 시간: {} 이전", threshold);

        for (int i = 0; i <= maxLoops; i++) {
            List<Long> dataIds = referenceRoomRepository.findExpiredDeletedDataIds(threshold, BATCH_SIZE);
            if (dataIds.isEmpty()) {
                break;
            }

            try {
                Integer deletedCounts = transactionTemplate.execute(status -> {
                    likeRepository.deleteByDataIds(dataIds);
                    rfCommentRepository.deleteByDataIds(dataIds);
                    attachmentRepository.deleteByDataIds(dataIds);
                    return referenceRoomRepository.deleteByIds(dataIds);
                });

                if (deletedCounts == null || deletedCounts == 0) {
                    break;
                }

                totalDeleted += deletedCounts;
                log.info("[Batch {}] {}개 삭제 완료 (누적: {})", i + 1, deletedCounts, totalDeleted);
            }
            catch (Exception e) {
                log.error("[삭제 처리 중 오류 발생] dataIds={}. 프로세스를 중단합니다.", dataIds, e);
                break;
            }
        }

        log.info("[삭제 1년 경과 자료실 정리 종료] 총 {}개 삭제됨", totalDeleted);
    }

    public void deleteUnusedFiles() {
        int totalDeleted = 0;
        int maxLoops = 5;

        log.info("[삭제되어 사용되지 않는 파일 정리 시작]");

        for (int i = 0; i <= maxLoops; i++) {
            List<Attachment> unusedAttachments = attachmentRepository.findUnusedAttachments(BATCH_SIZE);
            if (unusedAttachments.isEmpty()) {
                break;
            }

            int currentBatchDeleted = 0;
            for (Attachment attachment : unusedAttachments) {
                try {
                    // 실제 파일 삭제
                    String[] prefixAndId = parsePath(attachment.getPath());
                    fileStore.deleteAttactment(prefixAndId[0], prefixAndId[1], attachment);

                    // DB에서 삭제
                    attachmentRepository.delete(attachment);

                    currentBatchDeleted++;
                } catch (IOException e) {
                    log.error("[파일 정리 중 IO 오류] ID: {}, Path: {}, 사유: {}",
                                attachment.getId(), attachment.getPath(), e.getMessage());
                } catch (Exception e) {
                    log.error("[파일 정리 중 예상치 못한 오류] ID: {}", attachment.getId(), e);
                }
            }

            totalDeleted += currentBatchDeleted;
            log.info("[Batch {}] {}개 파일 처리 완료 (누적: {})", i + 1, currentBatchDeleted, totalDeleted);
        }

        log.info("[삭제되어 사용되지 않는 파일 정리 시작 종료] 총 {}개 파일 삭제됨", totalDeleted);
    }

    private static String[] parsePath(String path) {
        // "/api/category/id/..." -> "category(community 또는 referenceroom),id" 형태로 바꾼 뒤 split
        return path.replaceFirst("^/api/([^/]+)/(\\d+).*", "$1,$2").split(",");
    }
}
