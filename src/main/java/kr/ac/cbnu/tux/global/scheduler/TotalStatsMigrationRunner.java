package kr.ac.cbnu.tux.global.scheduler;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.repository.CommunityRepository;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.repository.ReferenceRoomRepository;
import kr.ac.cbnu.tux.global.utility.ScoreUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@Profile("migration") // ./gradlew bootRun --args='--spring.profiles.active=migration,...'로 실행
@RequiredArgsConstructor
public class TotalStatsMigrationRunner implements ApplicationRunner {

    private static final int BATCH_SIZE = 100;

    private final CommunityRepository communityRepository;
    private final ReferenceRoomRepository referenceRoomRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        migrateCommunity();
        migrateReferenceRoom();
    }

    private void migrateCommunity() {
        PageRequest pageRequest = PageRequest.of(0, BATCH_SIZE);

        while (true) {
            // Score가 0인 데이터를 다시 조회
            // (처리된 데이터는 score가 변하므로 다음 페이지가 아닌 0페이지를 반복 조회 가능)
            Slice<Community> postSlice = communityRepository.findByScore(0.0, pageRequest);

            if (postSlice.isEmpty())
                break;

            for (Community post : postSlice) {
                long totalLikes = post.getLikes().stream().filter(l -> !l.getDislike()).count();
                long totalDislikes = post.getLikes().stream().filter(l -> l.getDislike()).count();
                long totalComments = post.getComments().size();
                double score = ScoreUtils.calculateInitialScore(post.getCreatedDate(), totalLikes - totalDislikes);

                post.setTotalLikes(totalLikes);
                post.setTotalDislikes(totalDislikes);
                post.setTotalComments(totalComments);
                post.setScore(score);
            }

            // 영속성 컨텍스트 초기화 (Batch Insert/Update 효과)
            entityManager.flush();
            entityManager.clear();

            if (!postSlice.hasNext()) break;
        }
    }

    private void migrateReferenceRoom() {
        PageRequest pageRequest = PageRequest.of(0, BATCH_SIZE);

        while (true) {
            // Score가 0인 데이터를 다시 조회
            // (처리된 데이터는 score가 변하므로 다음 페이지가 아닌 0페이지를 반복 조회 가능)
            Slice<ReferenceRoom> dataSlice = referenceRoomRepository.findByScore(0.0, pageRequest);

            if (dataSlice.isEmpty())
                break;

            for (ReferenceRoom data : dataSlice) {
                long totalLikes = data.getLikes().stream().filter(l -> !l.getDislike()).count();
                long totalDislikes = data.getLikes().stream().filter(l -> l.getDislike()).count();
                long totalComments = data.getComments().size();
                double score = ScoreUtils.calculateInitialScore(data.getCreatedDate(), totalLikes - totalDislikes);

                data.setTotalLikes(totalLikes);
                data.setTotalDislikes(totalDislikes);
                data.setTotalComments(totalComments);
                data.setScore(score);
            }

            // 영속성 컨텍스트 초기화 (Batch Insert/Update 효과)
            entityManager.flush();
            entityManager.clear();

            if (!dataSlice.hasNext()) break;
        }
    }
}
