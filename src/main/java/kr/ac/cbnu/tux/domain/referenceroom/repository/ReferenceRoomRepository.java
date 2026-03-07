package kr.ac.cbnu.tux.domain.referenceroom.repository;

import jakarta.persistence.LockModeType;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ReferenceRoomRepository extends JpaRepository<ReferenceRoom, Long>, ReferenceRoomRepositoryDsl {

    Optional<ReferenceRoom> findByIdAndIsDeletedFalse(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM ReferenceRoom r
        WHERE r.id = :id
          AND r.isDeleted = false
    """)
    Optional<ReferenceRoom> findByIdAndIsDeletedFalseWithLock(Long id);

    Slice<ReferenceRoom> findByScore(double score, Pageable pageable);

    @Query(value = """
        SELECT id
        FROM reference_room
        WHERE is_deleted = 1 AND deleted_date < :threshold
        LIMIT :batchSize
    """, nativeQuery = true)
    List<Long> findExpiredDeletedDataIds(LocalDateTime threshold, int batchSize);

    @Modifying
    @Query(value = """
        DELETE FROM ReferenceRoom d
        WHERE d.id IN (:dataIds)
    """)
    int deleteByIds(List<Long> dataIds);
}
