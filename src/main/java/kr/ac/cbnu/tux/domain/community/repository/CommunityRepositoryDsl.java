package kr.ac.cbnu.tux.domain.community.repository;

import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommunityRepositoryDsl {

    Community findOneDsl(Long id);

    Page<Community> searchDsl(String query, SearchType searchType, List<CommunityPostType> categories, Pageable pageable);
}
