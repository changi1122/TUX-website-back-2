package kr.ac.cbnu.tux.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.entity.Community;
import kr.ac.cbnu.tux.entity.Like;
import kr.ac.cbnu.tux.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;


    @Transactional
    public void create(Community post, User user, Boolean dislike) throws Exception {
        if (!likeRepository.existsByPostAndUserAndDislike(post, user, dislike)) {
            Like like = new Like();
            like.setPost(post);
            like.setUser(user);
            like.setDislike(dislike);
            likeRepository.save(like);
        } else {
            throw new Exception("already like/dislike given");
        }
    }

    @Transactional
    public void create(ReferenceRoom data, User user, Boolean dislike) throws Exception {
        if (!likeRepository.existsByDataAndUserAndDislike(data, user, dislike)) {
            Like like = new Like();
            like.setData(data);
            like.setUser(user);
            like.setDislike(dislike);
            likeRepository.save(like);
        } else {
            throw new Exception("already like/dislike given");
        }
    }
}
