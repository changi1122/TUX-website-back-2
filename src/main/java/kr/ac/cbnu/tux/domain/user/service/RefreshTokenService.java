package kr.ac.cbnu.tux.domain.user.service;

import kr.ac.cbnu.tux.domain.user.dto.response.Token;
import kr.ac.cbnu.tux.domain.user.entity.RefreshToken;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.repository.RefreshTokenRepository;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 리프레시 토큰 로테이션: PESSIMISTIC_WRITE 락으로 동시 요청의 중복 처리를 방지
     * 성공 시 새 액세스 토큰 반환, 실패 시 empty 반환
     */
    @Transactional
    public Optional<RotationResult> rotate(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return Optional.empty();
        }

        String username = jwtTokenProvider.getUsernameFromJwt(refreshToken);

        // 행 수준 락으로 동시 요청 직렬화
        Optional<RefreshToken> storedToken =
                refreshTokenRepository.findByUsernameAndTokenForUpdate(username, hash(refreshToken));

        if (storedToken.isEmpty() || storedToken.get().isExpired()) {
            return Optional.empty();
        }

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );

        Token newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        Token newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenRepository.delete(storedToken.get());
        refreshTokenRepository.save(RefreshToken.builder()
                .token(hash(newRefreshToken.getToken()))
                .username(username)
                .expiryDate(OffsetDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshExpirationMs() / 1000
                ))
                .build());

        return Optional.of(new RotationResult(newAccessToken, newRefreshToken, user));
    }

    @Transactional
    public void deleteToken(String rawToken) {
        refreshTokenRepository.deleteByToken(hash(rawToken));
    }

    @Transactional
    public RotationResult issue(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );

        Token accessToken = jwtTokenProvider.generateAccessToken(authentication);
        Token refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenRepository.save(RefreshToken.builder()
                .token(hash(refreshToken.getToken()))
                .username(user.getUsername())
                .expiryDate(OffsetDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshExpirationMs() / 1000
                ))
                .build());

        return new RotationResult(accessToken, refreshToken, user);
    }

    public record RotationResult(Token accessToken, Token refreshToken, User user) {}

    static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}