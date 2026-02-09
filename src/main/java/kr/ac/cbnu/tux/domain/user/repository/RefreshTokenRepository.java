package kr.ac.cbnu.tux.domain.user.repository;

import kr.ac.cbnu.tux.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Transactional(readOnly = true)
    Optional<RefreshToken> findByToken(String token);

    @Transactional(readOnly = true)
    List<RefreshToken> findByUsername(String username);

    void deleteByUsername(String username);

    void deleteByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteExpiredTokens(OffsetDateTime now);
}
