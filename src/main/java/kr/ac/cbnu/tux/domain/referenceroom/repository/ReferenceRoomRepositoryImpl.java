package kr.ac.cbnu.tux.domain.referenceroom.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.ac.cbnu.tux.domain.common.enums.SearchType;
import kr.ac.cbnu.tux.domain.common.enums.SortType;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static kr.ac.cbnu.tux.domain.referenceroom.entity.QReferenceRoom.referenceRoom;

@Repository
@RequiredArgsConstructor
public class ReferenceRoomRepositoryImpl implements ReferenceRoomRepositoryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ReferenceRoom> findAllDsl(
            List<ReferenceRoomPostType> categories,
            String query,
            SearchType searchType,
            SortType sortType,
            Pageable pageable
    ) {
        BooleanExpression where = referenceRoom.isDeleted.isFalse()
                .and(buildCategoryPredicate(categories))
                .and(buildSearchPredicate(query, searchType));

        List<ReferenceRoom> content = jpaQueryFactory
                .selectFrom(referenceRoom)
                .join(referenceRoom.user)
                .where(where)
                .orderBy(getOrderBy(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(referenceRoom.count())
                .from(referenceRoom)
                .join(referenceRoom.user)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression buildSearchPredicate(String query, SearchType searchType) {
        return switch (searchType) {
            case TITLE -> referenceRoom.title.containsIgnoreCase(query);
            case BODY -> referenceRoom.body.containsIgnoreCase(query);
            case AUTHOR -> referenceRoom.isAnonymized.isFalse()
                    .and(referenceRoom.user.nickname.containsIgnoreCase(query));
            case ALL -> referenceRoom.title.containsIgnoreCase(query)
                    .or(referenceRoom.lecture.containsIgnoreCase(query))
                    .or(referenceRoom.professor.containsIgnoreCase(query))
                    .or(referenceRoom.body.containsIgnoreCase(query))
                    .or(referenceRoom.isAnonymized.isFalse()
                            .and(referenceRoom.user.nickname.containsIgnoreCase(query)));
        };
    }

    private BooleanExpression buildCategoryPredicate(List<ReferenceRoomPostType> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return referenceRoom.category.in(categories);
    }

    public static OrderSpecifier<?>[] getOrderBy(SortType sortType) {
        if (sortType == SortType.SCORE) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, referenceRoom.score),
                    new OrderSpecifier<>(Order.DESC, referenceRoom.createdDate)
            };
        }
        else if (sortType == SortType.LIKES) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, referenceRoom.totalLikes),
                    new OrderSpecifier<>(Order.DESC, referenceRoom.createdDate)
            };
        }
        else if (sortType == SortType.VIEW) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, referenceRoom.view),
                    new OrderSpecifier<>(Order.DESC, referenceRoom.createdDate)
            };
        }

        return new OrderSpecifier[] {
                new OrderSpecifier<>(Order.DESC, referenceRoom.createdDate)
        };
    }
}