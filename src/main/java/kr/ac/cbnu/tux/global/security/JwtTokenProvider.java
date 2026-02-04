package kr.ac.cbnu.tux.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.common.util.StringUtils;
import kr.ac.cbnu.tux.domain.user.dto.response.Token;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTokenProvider {

    private static final String JWT_SECRET = "wewklfjlwejfl;wjeflwejfl;kjsdc;ljeilwjrhfl;iwejf;lawjklfehklfhwkjehfkjer";
    private static final SecretKey JWT_SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    // Token Expiration Time
    private static final int JWT_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000;

    private final static JwtParser jwtParser;

    static {
        jwtParser = Jwts.parser()
                .verifyWith(JWT_SECRET_KEY)
                .build();
    }

    public static Token generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);

        return Token.of(
                Jwts.builder()
                        // username을 "sub"라는 claim으로 토큰에 추가
                        .subject(((UserDetails)authentication.getPrincipal()).getUsername())
                        // 회원 권한을 "role"이라는 claim으로 토큰에 추가
                        .claim("role", authentication.getAuthorities())
                        .issuedAt(now)
                        .expiration(expiryDate)
                        .signWith(JWT_SECRET_KEY, Jwts.SIG.HS512)
                        .compact(),
                expiryDate.getTime()
        );
    }

    public static String getUsernameFromJwt(String token) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    public static boolean validateToken(String token) {
        if (token == null || StringUtils.isEmpty(token))
            return false;

        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            //log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            //log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            //log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            //log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            //log.error("JWT claims string is empty.");
        }
        return false;
    }
}
