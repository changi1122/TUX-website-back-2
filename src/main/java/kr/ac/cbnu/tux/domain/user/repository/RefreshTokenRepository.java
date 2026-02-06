package kr.ac.cbnu.tux.domain.user.repository;

import kr.ac.cbnu.tux.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Transactional(readOnly = true)
    Optional<RefreshToken> findByToken(String token);

    @Transactional(readOnly = true)
    Optional<RefreshToken> findByUsername(String username);

    void deleteByUsername(String username);

    void deleteByToken(String token);
}
