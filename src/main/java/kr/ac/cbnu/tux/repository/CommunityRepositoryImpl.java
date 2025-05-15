package kr.ac.cbnu.tux.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.ac.cbnu.tux.domain.Community;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static kr.ac.cbnu.tux.domain.QCommunity.*;

@Repository
@RequiredArgsConstructor
public class CommunityRepositoryImpl implements CommunityRepositoryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Community findOneDsl(Long id) {
        return jpaQueryFactory
                .selectFrom(community)
                .where(
                        community.id.eq(id)
                                .and(community.isDeleted.isFalse())
                )
                .fetchOne();
    }


}
