package kr.ac.cbnu.tux.domain.community.entity;

import jakarta.persistence.*;
import kr.ac.cbnu.tux.domain.user.entity.User;
import lombok.*;

import java.time.OffsetDateTime;

// CommunityComment
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmComment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false)
    private OffsetDateTime createdDate;

    private OffsetDateTime deletedDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public void setUser(User user) {
        this.user = user;

        if (user != null && !user.getCmComments().contains(this)) {
            user.getCmComments().add(this);
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Community post;

    public void setPost(Community post) {
        this.post = post;

        if (post != null && !post.getComments().contains(this)) {
            post.getComments().add(this);
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CmComment parent;

    public void initializeComment(Community post, User user, OffsetDateTime now) {
        this.user = user;
        this.post = post;
        this.isDeleted = false;
        this.createdDate = now;
    }

    public void initializeComment(Community post, User user, OffsetDateTime now, CmComment parent) {
        initializeComment(post, user, now);
        this.parent = parent;
    }

    public void deleteComment(OffsetDateTime now) {
        this.isDeleted = true;
        this.deletedDate = now;
    }
}
