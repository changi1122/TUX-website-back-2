package kr.ac.cbnu.tux.global.utility;

import java.time.OffsetDateTime;

/**
 * [Ranking Algorithm: Logarithmic Scale with Relative Distance]
 * * 목적: 추천수 기반 정렬 시, 오래된 글은 감점하고 최근 추천 활동이 있는 글은 역주행하도록 설계.
 * 기준: 추천 10개가 쌓이면 약 12시간 전의 최신글과 대등한 점수를 가짐.
 * * 공식: Score = (작성시간 / 3600) + Σ (W * log10(1 + (현재시간 - 작성시간) / G))
 * * - Base Score: Unix Timestamp를 3600(1시간)으로 나눠 시간당 1점씩 감점되는 효과 부여.
 * - W (Weight): 추천 1개당 가중치 (4.0 설정 시 10개 추천이 약 12.04점 생성).
 * - G (Gravity): 시간 상향선 (43,200초 = 12시간). 추천 시점과 작성 시점의 거리를 조절.
 * - Log10: 추천이 폭발할 때 점수 상승폭을 완만하게 제한하여 어뷰징 방지.
 */

public class ScoreUtils {

    // 설계 상수
    private static final double WEIGHT = 4.0;
    private static final double GRAVITY = 43200.0;
    private static final long HOUR_IN_SECONDS = 3600;

    /**
     * 게시글 최초 생성 시 초기 랭킹 점수 계산
     * @param createdDate 작성 일시
     * @return 초기 랭킹 점수
     */
    public static double calculateInitialScore(OffsetDateTime createdDate) {
        // 유닉스 타임스탬프를 시간 단위로 변환하여 기본 점수 생성
        // 시간이 흐를수록 새로 생성되는 글의 타임스탬프가 커지므로 자연스럽게 최신글이 유리함
        return (double) createdDate.toEpochSecond() / HOUR_IN_SECONDS;
    }

    /**
     * 기존 게시글을 대상으로 초기 랭킹 점수 계산
     * @param createdDate 작성 일시
     * @param totalLikes 총 좋아요 개수 (좋아요 개수 - 싫어요 개수)
     * @return 초기 랭킹 점수
     */
    public static double calculateInitialScore(OffsetDateTime createdDate, Long totalLikes) {
        double baseScore = (double) createdDate.toEpochSecond() / HOUR_IN_SECONDS;

        if (totalLikes == 0) return baseScore;

        long now = OffsetDateTime.now().toEpochSecond();
        long postTime = createdDate.toEpochSecond();
        long timeDiff = Math.max(0, now - postTime);

        // 기존 추천들에 대한 일괄 가중치 계산
        // (작성 시점부터 지금까지의 중간 지점에서 추천이 발생했다고 가정하여 평균 점수 산출)
        double averageTimeFactor = timeDiff / 2.0;
        double bonusPerLike = WEIGHT * Math.log10(1.0 + (averageTimeFactor / GRAVITY));

        return baseScore + (totalLikes * bonusPerLike);
    }

    /**
     * 추천 발생 시 랭킹 점수 업데이트
     * @param currentScore 현재 DB에 저장된 score
     * @param createdDate 게시글 작성 일시
     * @param isDisliked 싫어요 여부
     * @return 업데이트된 랭킹 점수
     */
    public static double getUpdatedScoreOnLike(double currentScore, OffsetDateTime createdDate, boolean isDisliked) {
        long now = OffsetDateTime.now().toEpochSecond();
        long postTime = createdDate.toEpochSecond();

        // 현재 시간과 작성 시간의 차이 (초)
        long timeDiff = Math.max(0, now - postTime);

        // 로그 기반 상대적 거리 가중치 계산
        // 공식: W * log10(1 + timeDiff / G)
        double addedScore = WEIGHT * Math.log10(1.0 + (timeDiff / GRAVITY));

        return (!isDisliked) ? currentScore + addedScore : currentScore - addedScore;
    }
}
