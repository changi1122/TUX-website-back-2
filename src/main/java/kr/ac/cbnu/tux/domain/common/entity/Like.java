package kr.ac.cbnu.tux.domain.common.entity;

import jakarta.persistence.*;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.user.entity.User;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Boolean dislike; // false: 추천, true: 비추천

    @ManyToOne(fetch = FetchType.LAZY)
    private Community post;

    public void setPost(Community post) {
        this.post = post;

        if (post != null && !post.getLikes().contains(this)) {
            post.getLikes().add(this);
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    private ReferenceRoom data;

    public void setData(ReferenceRoom data) {
        this.data = data;

        if (data != null && !data.getLikes().contains(this)) {
            data.getLikes().add(this);
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
