package kr.ac.cbnu.tux.domain.referenceroom.repository;

import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// ReferenceRoomComment Repository
@Repository
public interface RfCommentRepository extends JpaRepository<RfComment, Long> {

    @Modifying
    @Query(value = """
        DELETE FROM rf_comment
        WHERE data_id IN (:dataIds)
    """, nativeQuery = true)
    int deleteByDataIds(List<Long> dataIds);
}
