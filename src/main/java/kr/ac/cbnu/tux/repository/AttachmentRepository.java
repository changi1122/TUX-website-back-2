package kr.ac.cbnu.tux.repository;

import kr.ac.cbnu.tux.domain.Attachment;
import kr.ac.cbnu.tux.domain.Community;
import kr.ac.cbnu.tux.domain.ReferenceRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Optional<Attachment> findByFilenameAndPost(String filename, Community post);

    Optional<Attachment> findByFilenameAndData(String filename, ReferenceRoom data);

    @Transactional
    @Query("update Attachment a set a.downloadCount = a.downloadCount + 1 where a.id = :id")
    @Modifying
    void increaseDownloadCountById(Long id);

    @Modifying
    @Query(value = """
        UPDATE attachment
        SET post_id = NULL
        WHERE post_id IN (:postIds)
    """, nativeQuery = true)
    int deleteByPostIds(List<Long> postIds);

    @Modifying
    @Query(value = """
        UPDATE attachment
        SET data_id = NULL
        WHERE data_id IN (:dataIds)
    """, nativeQuery = true)
    int deleteByDataIds(List<Long> dataIds);
}
