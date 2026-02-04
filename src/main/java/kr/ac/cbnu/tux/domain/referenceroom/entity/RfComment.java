package kr.ac.cbnu.tux.domain.referenceroom.entity;

import jakarta.persistence.*;
import kr.ac.cbnu.tux.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

// ReferenceRoom Comment
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfComment {

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

        if (user != null && !user.getRfComments().contains(this)) {
            user.getRfComments().add(this);
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_id")
    private ReferenceRoom data;

    public void setData(ReferenceRoom data) {
        this.data = data;

        if (data != null && !data.getComments().contains(this)) {
            data.getComments().add(this);
        }
    }

    public void initializeComment(ReferenceRoom data, User user, OffsetDateTime now) {
        this.user = user;
        this.data = data;
        this.createdDate = now;
        this.isDeleted = false;
    }

    public void deleteComment(OffsetDateTime now) {
        this.isDeleted = true;
        this.deletedDate = now;
    }
}
