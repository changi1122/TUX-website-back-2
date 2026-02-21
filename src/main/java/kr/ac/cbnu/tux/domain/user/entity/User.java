package kr.ac.cbnu.tux.domain.user.entity;

import jakarta.persistence.*;
import kr.ac.cbnu.tux.domain.community.entity.CmComment;
import kr.ac.cbnu.tux.domain.community.entity.Community;
import kr.ac.cbnu.tux.domain.referenceroom.entity.ReferenceRoom;
import kr.ac.cbnu.tux.domain.referenceroom.entity.RfComment;
import kr.ac.cbnu.tux.domain.user.dto.request.UserDataRequest;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;    // 로그인에 사용되는 아이디

    @Column(nullable = false)
    private String nickname;    // 외부로 공개되는 닉네임 (작성자명)

    @Setter
    @Column(nullable = false)
    private String password;    // 로그인에 사용되는 비밀번호 (암호화되어 저장됨)

    @Setter
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRole role;      // 회원 권한 등급 (게스트 / 유저 / 매니저 / 어드민)
    /*
    * 게스트 : 가입 직후 권한, 커뮤니티 읽기 가능/자료실 및 갤러리 사용불가 (어드민 권한의 사용자가 유저로 권한 변경 필요)
    * 유저 : 일반 회원, 커뮤니티와 자료실 쓰기읽기 가능/갤러리 읽기 가능
    * 매니저 : 어드민보다 낮은 권한의 관리자, 커뮤니티와 자료실, 갤러리 쓰기읽기삭제 가능
    * 어드민 : 최고 관리자, 매니저 권한 + 회원 권한 변경 가능
    */

    @Column(nullable = false)
    private String email;       // 이메일 주소

    private String department;      // 학과
    private String studentNumber;   // 학번
    private String phoneNumber;     // 전화번호

    @Column(nullable = false)
    private boolean isLocked;       // 잠김 여부 (미사용, 스프링시큐리티 요구사항)
    @Column(nullable = false)
    private boolean isBanned;       // 밴 여부
    @Column(nullable = false)
    private boolean isDeleted;      // 탈퇴여부

    @Column(nullable = false)
    private OffsetDateTime createdDate; // 가입일시
    private OffsetDateTime bannedDate;  // 밴 일시
    private OffsetDateTime deletedDate; // 탈퇴 일시

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.role.name()));
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return (!isLocked && !isBanned && !isDeleted);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 정책 미사용
    }

    @Override
    public boolean isEnabled() {
        return (!isLocked && !isBanned && !isDeleted);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        return Objects.equals(this.id, ((User) obj).getId());
    }

    /* 커뮤니티 글 */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Community> posts = new ArrayList<>();

    public void addPost(Community post) {
        this.posts.add(post);

        if (post.getUser() != this) {
            post.setUser(this);
        }
    }

    /* 커뮤니티 댓글 */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<CmComment> cmComments = new ArrayList<>();

    public void addCmComment(CmComment comment) {
        this.cmComments.add(comment);

        if (comment.getUser() != this) {
            comment.setUser(this);
        }
    }

    /* 자료실 글 */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<ReferenceRoom> datas = new ArrayList<>();

    public void addData(ReferenceRoom data) {
        this.datas.add(data);

        if (data.getUser() != this) {
            data.setUser(this);
        }
    }

    /* 자료실 댓글 */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<RfComment> rfComments = new ArrayList<>();

    public void addRfComment(RfComment comment) {
        this.rfComments.add(comment);

        if (comment.getUser() != this) {
            comment.setUser(this);
        }
    }

    public void initializeUser(OffsetDateTime now) {
        this.role = UserRole.GUEST;
        this.isLocked = false;
        this.isBanned = false;
        this.isDeleted = false;
        this.createdDate = now;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateUserData(UserDataRequest request) {
        if (request.getNickname() != null)
            this.nickname = request.getNickname();
        if (request.getEmail() != null)
            this.email = request.getEmail();
        if (request.getDepartment() != null)
            this.department = request.getDepartment();
        if (request.getStudentNumber() != null)
            this.studentNumber = request.getStudentNumber();
        if (request.getPhoneNumber() != null)
            this.phoneNumber = request.getPhoneNumber();
    }

    public void deleteUserData(OffsetDateTime now) {
        this.password = "-";
        this.email = "-@-";
        this.department = "-";
        this.phoneNumber = "-";
        this.isDeleted = true;
        this.deletedDate = now;
    }

    public void ban(OffsetDateTime now) {
        this.isBanned = true;
        this.bannedDate = now;
    }
}
