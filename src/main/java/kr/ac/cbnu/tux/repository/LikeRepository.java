package kr.ac.cbnu.tux.repository;

import kr.ac.cbnu.tux.entity.Community;
import kr.ac.cbnu.tux.entity.Like;
import kr.ac.cbnu.tux.entity.ReferenceRoom;
import kr.ac.cbnu.tux.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Boolean existsByPostAndUserAndDislike(Community post, User user, Boolean dislike);

    Boolean existsByDataAndUserAndDislike(ReferenceRoom data, User user, Boolean dislike);

    @Modifying
    @Query(value = """
        DELETE FROM likes
        WHERE post_id IN (:postIds)
    """,  nativeQuery = true)
    int deleteByPostIds(List<Long> postIds);

    @Modifying
    @Query(value = """
        DELETE FROM likes
        WHERE data_id IN (:dataIds)
    """,  nativeQuery = true)
    int deleteByDataIds(List<Long> dataIds);
}
