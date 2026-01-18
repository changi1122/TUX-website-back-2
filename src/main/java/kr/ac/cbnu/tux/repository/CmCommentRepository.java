package kr.ac.cbnu.tux.repository;

import kr.ac.cbnu.tux.entity.CmComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/* CommunityCommentRepository */
@Repository
public interface CmCommentRepository extends JpaRepository<CmComment, Long> {

    @Modifying
    @Query(value = """
        DELETE FROM cm_comment
        WHERE post_id IN (:postIds)
    """, nativeQuery = true)
    int deleteByPostIds(List<Long> postIds);
}
