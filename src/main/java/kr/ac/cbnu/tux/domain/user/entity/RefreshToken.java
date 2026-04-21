package kr.ac.cbnu.tux.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token_username", columnList = "username")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private OffsetDateTime expiryDate;

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiryDate);
    }
}
