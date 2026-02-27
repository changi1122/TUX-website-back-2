package kr.ac.cbnu.tux.domain.common.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.common.entity.Like;
import kr.ac.cbnu.tux.domain.common.repository.LikeRepository;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.common.exception.CommonErrorCode;
import kr.ac.cbnu.tux.domain.common.exception.CommonException;
import kr.ac.cbnu.tux.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;


    @Transactional
    public void createLike(Community post, User user, Boolean dislike) {
        if (!likeRepository.existsByPostAndUserAndDislike(post, user, dislike)) {
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .dislike(dislike)
                    .build();
            likeRepository.save(like);
        } else {
            throw new CommonException(CommonErrorCode.DUPLICATE_LIKE);
        }
    }

    @Transactional
    public void createLike(ReferenceRoom data, User user, Boolean dislike) {
        if (!likeRepository.existsByDataAndUserAndDislike(data, user, dislike)) {
            Like like = Like.builder()
                    .data(data)
                    .user(user)
                    .dislike(dislike)
                    .build();
            likeRepository.save(like);
        } else {
            throw new CommonException(CommonErrorCode.DUPLICATE_LIKE);
        }
    }
}
