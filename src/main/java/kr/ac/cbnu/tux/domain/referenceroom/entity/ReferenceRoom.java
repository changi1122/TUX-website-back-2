package kr.ac.cbnu.tux.domain.referenceroom.entity;

import jakarta.persistence.*;
import kr.ac.cbnu.tux.domain.common.entity.Attachment;
import kr.ac.cbnu.tux.domain.common.entity.Like;
import kr.ac.cbnu.tux.domain.referenceroom.enums.ReferenceRoomPostType;
import kr.ac.cbnu.tux.domain.user.entity.User;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

// 자료실
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(name = "ReferenceRoom.fetchUser", attributeNodes = @NamedAttributeNode("user"))
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


    @OneToMany(mappedBy = "data", fetch = FetchType.EAGER)
    @Builder.Default
    private List<RfComment> comments = new ArrayList<>();

    public void addComment(RfComment comment) {
        this.comments.add(comment);

        if (comment.getData() != this) {
            comment.setData(this);
        }
    }

    @OneToMany(mappedBy = "data", fetch = FetchType.EAGER)
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
}
