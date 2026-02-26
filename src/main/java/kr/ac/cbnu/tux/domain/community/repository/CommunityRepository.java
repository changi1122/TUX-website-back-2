package kr.ac.cbnu.tux.domain.community.repository;

import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface CommunityRepository extends JpaRepository<Community, Long>, CommunityRepositoryDsl {
    // 메서드 이름 길이가 긴데, 이게 맞는 건지는 -> QueryDSL로 개선하면 좋을 듯...

    Optional<Community> findByIdAndIsDeletedFalse(Long id);

    @EntityGraph("Community.fetchUser")
    Page<Community> findByIsDeletedFalseOrderByCreatedDateDesc(Pageable pageable);

    @EntityGraph("Community.fetchUser")
    Page<Community> findByIsDeletedFalseOrderByViewDescCreatedDateDesc(Pageable pageable);

    @EntityGraph("Community.fetchUser")
    Page<Community> findByIsDeletedFalseAndCategoryOrderByCreatedDateDesc(CommunityPostType type, Pageable pageable);

    @EntityGraph("Community.fetchUser")
    Page<Community> findByIsDeletedFalseAndCategoryInOrderByCreatedDateDesc(List<CommunityPostType> types, Pageable pageable);

    @EntityGraph("Community.fetchUser")
    List<Community> findAllByIsDeletedFalse();

    Long countByIsDeletedFalse();

    @Query(value = """
        SELECT id
        FROM community
        WHERE is_deleted = 1 AND deleted_date < :threshold
        LIMIT :batchSize
    """, nativeQuery = true)
    List<Long> findExpiredDeletedPostIds(LocalDateTime threshold, int batchSize);

    @Modifying
    @Query(value = """
        DELETE FROM Community p
        WHERE p.id IN (:postIds)
    """)
    int deleteByIds(List<Long> postIds);
}
