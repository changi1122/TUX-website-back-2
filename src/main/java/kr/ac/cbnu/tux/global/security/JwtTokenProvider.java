package kr.ac.cbnu.tux.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.common.util.StringUtils;
import kr.ac.cbnu.tux.domain.user.dto.response.Token;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpirationMs;
    @Getter
    private final long refreshExpirationMs;
    private final JwtParser jwtParser;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration.access}") long accessExpirationMs,
                            @Value("${jwt.expiration.refresh}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.jwtParser = Jwts.parser()
                .verifyWith(this.secretKey)
                .build();
    }

    // 액세스 토큰 생성
    public Token generateAccessToken(Authentication authentication) {
        return generateToken(
                ((UserDetails) authentication.getPrincipal()).getUsername(),
                authentication.getAuthorities(),
                accessExpirationMs
        );
    }

    // 리프레시 토큰 생성
    public Token generateRefreshToken(Authentication authentication) {
        return generateToken(
                ((UserDetails) authentication.getPrincipal()).getUsername(),
                null, // 리프레시 토큰에는 role 불필요
                refreshExpirationMs
        );
    }

    private Token generateToken(String username, Object authorities, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        JwtBuilder builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey);

        if (authorities != null) {
            builder.claim("role", authorities);
        }

        return Token.of(builder.compact(), expiryDate.getTime());
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        if (token == null || StringUtils.isEmpty(token))
            return true;

        try {
            jwtParser.parseSignedClaims(token);
            return false;
        } catch (ExpiredJwtException ex) {
            return true;  // 만료됨
        } catch (Exception ex) {
            return false; // 만료가 아닌 다른 이유로 유효하지 않음
        }
    }

    // 토큰 유효 여부 확인
    public boolean validateToken(String token) {
        if (token == null || StringUtils.isEmpty(token))
            return false;

        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.debug("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.debug("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.debug("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.debug("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.debug("JWT claims string is empty.");
        }
        return false;
    }
}
