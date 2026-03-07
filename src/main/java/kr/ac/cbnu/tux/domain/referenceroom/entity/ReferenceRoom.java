package kr.ac.cbnu.tux.domain.referenceroom.entity;

import jakarta.persistence.*;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.entity.Like;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.global.utility.ScoreUtils;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

// 자료실
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(name = "ReferenceRoom.fetchUser", attributeNodes = @NamedAttributeNode("user"))
@Table(
        name = "reference_room",
        indexes = {
                @Index(name = "reference_room_list", columnList = "is_deleted, created_date"),
                @Index(name = "reference_room_list_by_category", columnList = "is_deleted, category, created_date"),
                @Index(name = "reference_room_list_by_score", columnList = "is_deleted, score"),
                @Index(name = "reference_room_list_by_category_and_score", columnList = "is_deleted, category, score"),
                @Index(name = "reference_room_list_by_category_and_likes", columnList = "is_deleted, category, total_likes")
        }
)
public class ReferenceRoom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ReferenceRoomPostType category;

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

    @Column(columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long totalLikes;

    @Column(columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long totalDislikes;

    @Setter
    @Column(columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long totalComments;

    @Column(columnDefinition = "DOUBLE NOT NULL DEFAULT 0")
    private Double score;
    
    /* 자료실 특수 정보 */
    @Column(nullable = false)
    private Boolean isAnonymized;   // 익명 여부

    private String lecture;         // 강의 이름
    private String semester;        // 학기
    private String professor;       // 교수
    

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public void setUser(User user) {
        this.user = user;

        if (user != null && !user.getDatas().contains(this)) {
            user.getDatas().add(this);
        }
    }


    @OneToMany(mappedBy = "data", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<RfComment> comments = new ArrayList<>();

    public void addComment(RfComment comment) {
        this.comments.add(comment);

        if (comment.getData() != this) {
            comment.setData(this);
        }
    }

    @OneToMany(mappedBy = "data", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    public void addAttachment(Attachment file) {
        this.attachments.add(file);

        if (file.getData() != this) {
            file.setData(this);
        }
    }

    public void removeAttachment(Attachment file) {
        this.attachments.remove(file);

        if (file.getData() != null) {
            file.setData(null);
        }
    }


    @OneToMany(mappedBy = "data", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Like> likes = new ArrayList<>();

    public void addlikes(Like like) {
        this.likes.add(like);

        if (like.getData() != this) {
            like.setData(this);
        }
    }

    public void initializeData(User user, OffsetDateTime now) {
        this.isDeleted = false;
        this.createdDate = now;
        this.view = 0L;
        this.totalLikes = 0L;
        this.totalDislikes = 0L;
        this.totalComments = 0L;
        this.score = ScoreUtils.calculateInitialScore(now);
        this.user = user;
    }

    public void updateTemporalData(ReferenceRoomPostType type, String title, short editorVersion, boolean isAnonymized,
                                   String lecture, String semester, String professor, OffsetDateTime now) {
        this.category = (type != null) ? type : this.category;
        this.title = title;
        this.createdDate = now;
        this.isDeleted = false;
        this.editorVersion = editorVersion;
        this.isAnonymized = isAnonymized;
        this.lecture = lecture;
        this.semester = semester;
        this.professor = professor;
    }

    public void updateData(ReferenceRoomPostType type, String title, short editorVersion, boolean isAnonymized,
                           String lecture, String semester, String professor, OffsetDateTime now) {
        this.category = type;
        this.title = title;
        this.editorVersion = editorVersion;
        this.isAnonymized = isAnonymized;
        this.lecture = lecture;
        this.semester = semester;
        this.professor = professor;
        this.editedDate = now;
    }

    public void deleteData(OffsetDateTime now) {
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
