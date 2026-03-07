package kr.ac.cbnu.tux.domain.community.entity;

import jakarta.persistence.*;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.entity.Like;
import kr.ac.cbnu.tux.domain.community.enums.CommunityPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.global.utility.ScoreUtils;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(name = "Community.fetchUser", attributeNodes = @NamedAttributeNode("user"))
@Table(
        name = "community",
        indexes = {
                @Index(name = "community_list", columnList = "is_deleted, created_date"),
                @Index(name = "community_list_by_category", columnList = "is_deleted, category, created_date"),
                @Index(name = "community_list_by_score", columnList = "is_deleted, score"),
                @Index(name = "community_list_by_category_and_score", columnList = "is_deleted, category, score"),
                @Index(name = "community_list_by_category_and_likes", columnList = "is_deleted, category, total_likes")
        }
)
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CommunityPostType category;

    @Column(nullable = false)
    private String title;

    @Setter
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String body;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false)
    private OffsetDateTime createdDate;

    private OffsetDateTime editedDate;

    private OffsetDateTime deletedDate;

    private Short editorVersion;

    @Column(nullable = false)
    private Long view;

    @Setter
    @Column(columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long totalLikes;

    @Setter
    @Column(columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long totalDislikes;

    @Setter
    @Column(columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long totalComments;

    @Column(columnDefinition = "DOUBLE NOT NULL DEFAULT 0")
    private Double score;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public void setUser(User user) {
        this.user = user;

        if (user != null && !user.getPosts().contains(this)) {
            user.getPosts().add(this);
        }
    }

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<CmComment> comments = new ArrayList<>();

    public void addComment(CmComment comment) {
        this.comments.add(comment);

        if (comment.getPost() != this) {
            comment.setPost(this);
        }
    }

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    public void addAttachment(Attachment file) {
        this.attachments.add(file);

        if (file.getPost() != this) {
            file.setPost(this);
        }
    }

    public void removeAttachment(Attachment file) {
        this.attachments.remove(file);

        if (file.getData() != null) {
            file.setData(null);
        }
    }


    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Like> likes = new ArrayList<>();

    public void addlikes(Like like) {
        this.likes.add(like);

        if (like.getPost() != this) {
            like.setPost(this);
        }
    }

    public void initializePost(User user, OffsetDateTime now) {
        this.createdDate = now;
        this.isDeleted = false;
        this.view = 0L;
        this.totalLikes = 0L;
        this.totalDislikes = 0L;
        this.totalComments = 0L;
        this.score = ScoreUtils.calculateInitialScore(now);
        this.user = user;
    }

    public void updateTemporalPost(CommunityPostType type, String title, short editorVersion, OffsetDateTime now) {
        this.category = (type != null) ? type : this.category;
        this.title = title;
        this.createdDate = now;
        this.isDeleted = false;
        this.editorVersion = editorVersion;
    }

    public void updatePost(CommunityPostType type, String title, short editorVersion, OffsetDateTime now) {
        this.category = type;
        this.title = title;
        this.editorVersion = editorVersion;
        this.editedDate = now;
    }

    public void deletePost(OffsetDateTime now) {
        this.isDeleted = true;
        this.deletedDate = now;
    }

    public void likePost(boolean isDisliked, OffsetDateTime now) {
        if (isDisliked)
            this.totalDislikes++;
        else
            this.totalLikes++;
        this.score = ScoreUtils.getUpdatedScoreOnLike(this.score, now, isDisliked);
    }

    public void createComment() {
        this.totalComments++;
    }

    public void deleteComment() {
        this.totalComments--;
    }
}
