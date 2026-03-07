package kr.ac.cbnu.tux.domain.community.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.common.enums.SortType;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static kr.ac.cbnu.tux.domain.community.entity.QCommunity.community;

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

    @Override
    public Page<Community> findAllDsl(
            List<CommunityPostType> categories,
            String query,
            SearchType searchType,
            SortType sortType,
            Pageable pageable
    ) {

        BooleanExpression where = community.isDeleted.isFalse()
                .and(buildCategoryPredicate(categories))
                .and(buildSearchPredicate(query, searchType));

        List<Community> content = jpaQueryFactory
                .selectFrom(community)
                .join(community.user)
                .where(where)
                .orderBy(getOrderBy(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(community.count())
                .from(community)
                .join(community.user)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression buildSearchPredicate(String query, SearchType searchType) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return switch (searchType) {
            case TITLE -> community.title.containsIgnoreCase(query);
            case BODY -> community.body.containsIgnoreCase(query);
            case AUTHOR -> community.user.nickname.containsIgnoreCase(query);
            case ALL -> community.title.containsIgnoreCase(query)
                    .or(community.body.containsIgnoreCase(query))
                    .or(community.user.nickname.containsIgnoreCase(query));
        };
    }

    private BooleanExpression buildCategoryPredicate(List<CommunityPostType> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return community.category.in(categories);
    }

    public static OrderSpecifier<?>[] getOrderBy(SortType sortType) {
        if (sortType == SortType.SCORE) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, community.score),
                    new OrderSpecifier<>(Order.DESC, community.createdDate)
            };
        }
        else if (sortType == SortType.LIKES) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, community.totalLikes),
                    new OrderSpecifier<>(Order.DESC, community.createdDate)
            };
        }
        else if (sortType == SortType.VIEW) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, community.view),
                    new OrderSpecifier<>(Order.DESC, community.createdDate)
            };
        }

        return new OrderSpecifier[] {
                new OrderSpecifier<>(Order.DESC, community.createdDate)
        };
    }
}
