package kr.ac.cbnu.tux.domain.referenceroom.repository;

import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.common.enums.SortType;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReferenceRoomRepositoryDsl {

    Page<ReferenceRoom> findAllDsl(List<ReferenceRoomPostType> categories, String query, SearchType searchType, SortType sortType, Pageable pageable);
}