package kr.ac.cbnu.tux.domain.common.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.common.entity.Like;
import kr.ac.cbnu.tux.domain.common.repository.LikeRepository;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.community.exception.CommunityErrorCode;
import kr.ac.cbnu.tux.domain.community.exception.CommunityException;
import kr.ac.cbnu.tux.domain.community.repository.CommunityRepository;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.common.exception.CommonErrorCode;
import kr.ac.cbnu.tux.domain.common.exception.CommonException;
import kr.ac.cbnu.tux.domain.referenceroom.exception.ReferenceRoomErrorCode;
import kr.ac.cbnu.tux.domain.referenceroom.exception.ReferenceRoomException;
import kr.ac.cbnu.tux.domain.referenceroom.repository.ReferenceRoomRepository;
import kr.ac.cbnu.tux.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final CommunityRepository communityRepository;
    private final ReferenceRoomRepository referenceRoomRepository;

    @Transactional
    public void createLikeOnCommunity(Long postId, User user, Boolean isDisliked, OffsetDateTime now) {
        Community post = communityRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.NOT_FOUND));

        if (likeRepository.existsByPostAndUserAndDislike(post, user, isDisliked)) {
            throw new CommonException(CommonErrorCode.DUPLICATE_LIKE);
        }

        Like like = Like.builder()
                .post(post)
                .user(user)
                .dislike(isDisliked)
                .build();
        likeRepository.save(like);

        post.likePost(isDisliked, now);
        communityRepository.save(post);
    }

    @Transactional
    public void createLikeOnReferenceRoom(Long dataId, User user, Boolean isDisliked, OffsetDateTime now) {
        ReferenceRoom data = referenceRoomRepository.findByIdAndIsDeletedFalse(dataId)
                .orElseThrow(() -> new ReferenceRoomException(ReferenceRoomErrorCode.NOT_FOUND));

        if (likeRepository.existsByDataAndUserAndDislike(data, user, isDisliked)) {
            throw new CommonException(CommonErrorCode.DUPLICATE_LIKE);
        }

        Like like = Like.builder()
                .data(data)
                .user(user)
                .dislike(isDisliked)
                .build();
        likeRepository.save(like);

        data.likePost(isDisliked, now);
        referenceRoomRepository.save(data);
    }
}
