package kr.ac.cbnu.tux.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.common.util.StringUtils;
import kr.ac.cbnu.tux.domain.user.dto.response.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;
    private final JwtParser jwtParser;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.jwtParser = Jwts.parser()
                .verifyWith(this.secretKey)
                .build();
    }

    public Token generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Token.of(
                Jwts.builder()
                        // username을 "sub"라는 claim으로 토큰에 추가
                        .subject(((UserDetails)authentication.getPrincipal()).getUsername())
                        // 회원 권한을 "role"이라는 claim으로 토큰에 추가
                        .claim("role", authentication.getAuthorities())
                        .issuedAt(now)
                        .expiration(expiryDate)
                        .signWith(secretKey)
                        .compact(),
                expiryDate.getTime()
        );
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        if (token == null || StringUtils.isEmpty(token))
            return false;

        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.info("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.info("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.info("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.info("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.info("JWT claims string is empty.");
        }
        return false;
    }
}
